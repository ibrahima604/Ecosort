package com.example.ecosort;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class PlasticClassifier {

    private static final String TAG        = "PlasticClassifier";
    private static final String MODEL_FILE = "model_plastique.tflite";
    private static final int    IMG_SIZE   = 224;

    private Interpreter interpreter;

    public PlasticClassifier(Context context) {
        try {
            interpreter = new Interpreter(loadModel(context));
            Log.d(TAG, "✅ Modèle TFLite chargé avec succès");
        } catch (IOException e) {
            Log.e(TAG, "❌ Erreur chargement modèle", e);
        }
    }

    private MappedByteBuffer loadModel(Context context) throws IOException {
        android.content.res.AssetFileDescriptor fd =
                context.getAssets().openFd(MODEL_FILE);
        FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
        FileChannel channel = fis.getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY,
                fd.getStartOffset(), fd.getDeclaredLength());
    }

    /**
     * Prédit si l'image contient du plastique.
     * Pixels envoyés bruts : valeurs float 0.0 à 255.0 SANS normalisation.
     * @param bitmap image source (n'importe quelle taille)
     * @return score entre 0.0 et 1.0
     */
    public float predict(Bitmap bitmap) {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter null — modèle non chargé");
            return -1f;
        }

        // 1. Redimensionner à 224x224
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true);

        // 2. ByteBuffer float32 : 1 * 224 * 224 * 3 * 4 bytes
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE * 3);
        inputBuffer.order(ByteOrder.nativeOrder());
        inputBuffer.rewind();

        // 3. Extraire les pixels et les mettre BRUTS (0-255) en float
        int[] pixels = new int[IMG_SIZE * IMG_SIZE];
        resized.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE);

        for (int pixel : pixels) {
            float r = (float)((pixel >> 16) & 0xFF); // 0-255 brut
            float g = (float)((pixel >>  8) & 0xFF); // 0-255 brut
            float b = (float)( pixel        & 0xFF); // 0-255 brut
            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }

        // 4. Inférence
        float[][] output = new float[1][1];
        interpreter.run(inputBuffer, output);

        float score = output[0][0];
        Log.d(TAG, "Score brut TFLite : " + score + " → " + Math.round(score * 100) + "%");
        return score;
    }

    /**
     * Logique métier :
     *  < 50%  → Non plastique
     * 50-75%  → Incertain, reprendre la photo
     * 75-100% → Plastique détecté
     */
    public static Result interpret(float score) {
        int pct = Math.round(score * 100);

        if (score < 0f)    return new Result("❌ Modèle non chargé",                          pct, Type.ERROR);
        if (score < 0.50f) return new Result("✅ Non plastique (" + pct + "%)",                pct, Type.NOT_PLASTIC);
        if (score < 0.75f) return new Result("🔄 Incertain (" + pct + "%) — Reprends la photo", pct, Type.UNCERTAIN);
        return                    new Result("⚠️ Plastique détecté (" + pct + "%)",            pct, Type.PLASTIC);
    }

    public enum Type { PLASTIC, NOT_PLASTIC, UNCERTAIN, ERROR }

    public static class Result {
        public final String message;
        public final int    percent;
        public final Type   type;

        Result(String message, int percent, Type type) {
            this.message = message;
            this.percent = percent;
            this.type    = type;
        }
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            Log.d(TAG, "Interpreter fermé");
        }
    }
}