package com.example.mindu;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.mindu.api.RetrofitClient;
import com.example.mindu.api.login.MinhaContaResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Camera/gallery flow shared by the profile screens. Local profiles never upload a photo. */
public final class ProfilePhotoController {
    private static final String STATE_CAPTURE_URI = "profile_capture_uri";
    private static final long MAX_BYTES = 5L * 1024L * 1024L;
    private static final ExecutorService IMAGE_EXECUTOR = Executors.newSingleThreadExecutor();

    private final AppCompatActivity activity;
    private final boolean professional;
    private final ActivityResultLauncher<String> cameraPermission;
    private final ActivityResultLauncher<Uri> camera;
    private final ActivityResultLauncher<String> gallery;
    private ImageView image;
    private TextView initials;
    private Uri captureUri;
    private String userId;
    private boolean remotePhotoExists;

    public ProfilePhotoController(AppCompatActivity activity, boolean professional, Bundle state) {
        this.activity = activity;
        this.professional = professional;
        if (state != null) {
            String saved = state.getString(STATE_CAPTURE_URI);
            if (saved != null) captureUri = Uri.parse(saved);
        }
        cameraPermission = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) startCamera();
                    else showCameraPermissionHelp();
                });
        camera = activity.registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && captureUri != null) processImage(captureUri);
            else Toast.makeText(activity, "Captura cancelada.", Toast.LENGTH_SHORT).show();
        });
        gallery = activity.registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) processImage(uri);
        });
        userId = SessionManager.getUserId(activity);
    }

    public void onSaveInstanceState(Bundle out) {
        if (captureUri != null) out.putString(STATE_CAPTURE_URI, captureUri.toString());
    }

    public void bind(View avatarArea, ImageView imageView, TextView initialsView, String name) {
        image = imageView;
        initials = initialsView;
        userId = SessionManager.getUserId(activity);
        if (name != null && !name.trim().isEmpty()) initials.setText(name.trim().substring(0, 1).toUpperCase());
        bindClickTargets(avatarArea);
        image.setClipToOutline(true);
        image.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override public void getOutline(View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        if (userId == null || userId.trim().isEmpty()) fetchIdentity(); else loadPhoto();
    }

    private void bindClickTargets(View view) {
        view.setOnClickListener(v -> showMenu());
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) bindClickTargets(group.getChildAt(i));
        }
    }

    private void fetchIdentity() {
        fetchIdentity(null);
    }

    private void fetchIdentity(Runnable afterLoad) {
        RetrofitClient.getApiService(activity).minhaConta().enqueue(new Callback<MinhaContaResponse>() {
            @Override public void onResponse(Call<MinhaContaResponse> call, Response<MinhaContaResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getId() == null) return;
                MinhaContaResponse me = response.body();
                userId = me.getId();
                SessionManager.save(activity, SessionManager.getToken(activity), me.getTipo(), me.getStatus(), userId);
                if (afterLoad == null) loadPhoto(); else afterLoad.run();
            }
            @Override public void onFailure(Call<MinhaContaResponse> call, Throwable t) { }
        });
    }

    private void loadPhoto() {
        if (image == null || initials == null || activity.isFinishing()) return;
        if (professional) {
            RetrofitClient.getApiService(activity).foto(userId).enqueue(new Callback<ResponseBody>() {
                @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (!response.isSuccessful() || response.body() == null) { remotePhotoExists = false; showInitials(); return; }
                    remotePhotoExists = true;
                    ResponseBody body = response.body();
                    IMAGE_EXECUTOR.execute(() -> {
                        Bitmap bitmap = null;
                        try { bitmap = BitmapFactory.decodeStream(body.byteStream()); } catch (Exception ignored) { }
                        finally { body.close(); }
                        showBitmap(bitmap);
                    });
                }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) { showInitials(); }
            });
            return;
        }
        File photo = localPhoto();
        if (photo == null || !photo.isFile()) { showInitials(); return; }
        IMAGE_EXECUTOR.execute(() -> {
            Bitmap bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
            showBitmap(bitmap);
        });
    }

    private void showMenu() {
        String[] options = (localPhotoExists() || professional) ? new String[]{"Tirar foto", "Escolher da galeria", "Remover foto"}
                : new String[]{"Tirar foto", "Escolher da galeria"};
        new AlertDialog.Builder(activity).setTitle("Foto de perfil").setItems(options, (dialog, which) -> {
            String option = options[which];
            if ("Tirar foto".equals(option)) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera();
                else cameraPermission.launch(Manifest.permission.CAMERA);
            } else if ("Escolher da galeria".equals(option)) gallery.launch("image/*");
            else removePhoto();
        }).show();
    }

    private void startCamera() {
        try {
            File temp = new File(activity.getCacheDir(), "profile_capture_" + safeId() + ".jpg");
            if (!temp.createNewFile() && !temp.isFile()) throw new IOException("temp");
            captureUri = FileProvider.getUriForFile(activity, "com.example.mindu.fileprovider", temp);
            camera.launch(captureUri);
        } catch (Exception e) {
            Toast.makeText(activity, "Não foi possível abrir a câmera. Escolha uma imagem da galeria.", Toast.LENGTH_LONG).show();
        }
    }

    private void showCameraPermissionHelp() {
        new AlertDialog.Builder(activity).setMessage("A permissão da câmera foi negada. Você pode escolher uma imagem da galeria ou abrir as configurações para permitir a câmera.")
                .setNegativeButton("Agora não", null)
                .setNeutralButton("Galeria", (d, w) -> gallery.launch("image/*"))
                .setPositiveButton("Abrir configurações", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                }).show();
    }

    private void processImage(Uri source) {
        if (userId == null || userId.trim().isEmpty()) { fetchIdentity(() -> processImage(source)); return; }
        IMAGE_EXECUTOR.execute(() -> {
            try {
                byte[] jpeg = readCompressedJpeg(source);
                if (professional) uploadProfessionalPhoto(jpeg); else saveLocalPhoto(jpeg);
            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Não foi possível processar essa imagem.", Toast.LENGTH_LONG).show());
            }
        });
    }

    private byte[] readCompressedJpeg(Uri uri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
        try (InputStream input = activity.getContentResolver().openInputStream(uri)) { BitmapFactory.decodeStream(input, null, bounds); }
        int sample = 1; while (bounds.outWidth / sample > 1600 || bounds.outHeight / sample > 1600) sample *= 2;
        BitmapFactory.Options options = new BitmapFactory.Options(); options.inSampleSize = sample;
        Bitmap bitmap;
        try (InputStream input = activity.getContentResolver().openInputStream(uri)) { bitmap = BitmapFactory.decodeStream(input, null, options); }
        if (bitmap == null) throw new IOException("invalid image");
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try (InputStream input = activity.getContentResolver().openInputStream(uri)) {
            if (input != null) orientation = new ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception ignored) { }
        Matrix matrix = new Matrix();
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) matrix.postRotate(90);
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) matrix.postRotate(180);
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) matrix.postRotate(270);
        else if (orientation == ExifInterface.ORIENTATION_FLIP_HORIZONTAL) matrix.preScale(-1, 1);
        else if (orientation == ExifInterface.ORIENTATION_FLIP_VERTICAL) matrix.preScale(1, -1);
        else if (orientation == ExifInterface.ORIENTATION_TRANSPOSE) { matrix.preRotate(90); matrix.preScale(-1, 1); }
        else if (orientation == ExifInterface.ORIENTATION_TRANSVERSE) { matrix.preRotate(270); matrix.preScale(-1, 1); }
        if (!matrix.isIdentity()) { Bitmap fixed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); if (fixed != bitmap) bitmap.recycle(); bitmap = fixed; }
        int quality = 88;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while (true) {
            out.reset(); bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            if (out.size() <= MAX_BYTES) break;
            if (quality > 56) quality -= 8;
            else {
                int width = Math.max(1, (int) (bitmap.getWidth() * .75f));
                int height = Math.max(1, (int) (bitmap.getHeight() * .75f));
                Bitmap smaller = Bitmap.createScaledBitmap(bitmap, width, height, true);
                bitmap.recycle(); bitmap = smaller; quality = 80;
                if (width < 200 || height < 200) throw new IOException("image too large");
            }
        }
        bitmap.recycle();
        return out.toByteArray();
    }

    private void saveLocalPhoto(byte[] jpeg) throws IOException {
        File target = localPhoto(); if (target == null) throw new IOException("id unavailable");
        File temp = new File(target.getParentFile(), target.getName() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(temp)) { out.write(jpeg); out.flush(); out.getFD().sync(); }
        if (!temp.renameTo(target)) { temp.delete(); throw new IOException("save failed"); }
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length); showBitmap(bitmap);
    }

    private void uploadProfessionalPhoto(byte[] jpeg) {
        RequestBody body = RequestBody.create(jpeg, MediaType.parse("image/jpeg"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("foto", "perfil.jpg", body);
        RetrofitClient.getApiService(activity).atualizarFotoProfissional(userId, part).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.body() != null) response.body().close();
                if (response.isSuccessful()) loadPhoto();
                else Toast.makeText(activity, "A API não aceitou a foto. Tente novamente.", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) { Toast.makeText(activity, "Não foi possível enviar a foto agora.", Toast.LENGTH_LONG).show(); }
        });
    }

    private void removePhoto() {
        if (professional) { Toast.makeText(activity, "A remoção da foto profissional pela API não está disponível.", Toast.LENGTH_LONG).show(); return; }
        File file = localPhoto();
        if (file != null) file.delete();
        showInitials();
    }

    private boolean localPhotoExists() { File file = localPhoto(); return file != null && file.isFile(); }
    private File localPhoto() {
        if (userId == null || !userId.matches("[A-Za-z0-9_-]{1,100}")) return null;
        File directory = new File(activity.getFilesDir(), "profile_photos");
        if (!directory.exists()) directory.mkdirs();
        return new File(directory, "profile_" + userId + ".jpg");
    }
    private String safeId() { return userId != null && userId.matches("[A-Za-z0-9_-]{1,100}") ? userId : "account"; }
    private void showInitials() {
        if (image == null || initials == null) return;
        activity.runOnUiThread(() -> { image.setImageDrawable(null); image.setVisibility(View.GONE); initials.setVisibility(View.VISIBLE); });
    }
    private void showBitmap(Bitmap bitmap) {
        if (bitmap == null || image == null || initials == null) { showInitials(); return; }
        activity.runOnUiThread(() -> { if (!activity.isFinishing()) { image.setImageBitmap(bitmap); image.setVisibility(View.VISIBLE); initials.setVisibility(View.GONE); } });
    }

}
