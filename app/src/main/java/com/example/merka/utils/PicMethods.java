package com.example.merka.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class PicMethods {

    public static Uri getImageUri(Context inContext, Bitmap inImage, ImageView pic) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 30, bytes);
        inImage.compress(Bitmap.CompressFormat.PNG, 30, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);

        pic.setImageBitmap(inImage);

        return Uri.parse(path);
    }
}
