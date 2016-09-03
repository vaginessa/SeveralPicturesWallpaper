/*
 *      Copyright 2016 Jean-Pierre Hotz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.jeanpierrehotz.severalpictureswallpaper.wallpaper.data;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import com.blunderer.materialdesignlibrary.views.CardView;

import java.util.ArrayList;

/**
 *
 */
public class WallpaperImage{

    private static final int SAMPLE_WIDTH = 450;
    private static final int SAMPLE_HEIGHT = 800;

    private static final String PATH_AT = "This is the image at index ";
    private static final String TOTAL_NUMBER = "This is the length of the pictures.";

    private final String mPath;
    private Bitmap mImage;

    private int x;
    private int y;

    public WallpaperImage(String path){
        mPath = path;
    }

    public void loadImage(){
        if(mImage == null){
            Bitmap immutableBitMap = BitmapFactory.decodeFile(mPath);
            // make the loaded Bitmap to be mutable :)
            mImage = immutableBitMap.copy(Bitmap.Config.ARGB_8888, true);

            x = mImage.getWidth();
            y = mImage.getHeight();
        }
    }

    public void loadAsPreview(CardView toLoadTo){
        if(mImage == null){
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mPath, opt);

            x = opt.outWidth;
            y = opt.outHeight;

            PreviewLoaderTask task = new PreviewLoaderTask(toLoadTo);
            task.execute(x, y, SAMPLE_WIDTH, SAMPLE_HEIGHT);
        }
    }

    public void releaseImage(){
        if(mImage != null){
            mImage.recycle();
            mImage = null;
        }
    }

    public String getResolution(){
        if(mImage != null || (x != 0 && y != 0)){
            return String.format("%1$d x %2$dpx", x, y);
        }else{
            return "0 x 0px";
        }
    }

    public String getFileName(){
        return mPath.substring(mPath.lastIndexOf("/") + 1, mPath.lastIndexOf("."));
    }

    public void drawImage(float x, float y, Canvas c, Paint p){
        c.drawBitmap(mImage, new Rect(0, 0, mImage.getWidth(), mImage.getHeight()), new RectF(0, 0, x, y), p);
    }

    public static void saveToSharedPreferences(ArrayList<WallpaperImage> imgs, SharedPreferences prefs){
        SharedPreferences.Editor edit = prefs.edit().clear().putInt(TOTAL_NUMBER, imgs.size());

        for(int i = 0; i < imgs.size(); i++){
            edit.putString(PATH_AT + i, imgs.get(i).mPath);
        }

        edit.apply();
    }

    public static ArrayList<WallpaperImage> loadFromSharedPreferences(SharedPreferences prefs){
        int length = prefs.getInt(TOTAL_NUMBER, 0);
        ArrayList<WallpaperImage> images = new ArrayList<>();

        for(int i = 0; i < length; i++){
            images.add(new WallpaperImage(prefs.getString(PATH_AT + i, "")));
        }

        return images;
    }

    class PreviewLoaderTask extends AsyncTask<Integer, Void, Bitmap>{

        private CardView cardViewReference;

        public PreviewLoaderTask(CardView v){
            cardViewReference = v;
        }

        /**
         * 0 -> currX
         * 1 -> currY
         * 2 -> targetX
         * 3 -> targetY
         * @param integers asd
         * @return asd
         */
        @Override
        protected Bitmap doInBackground(Integer... integers){
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = calculateInSampleSize(x, y, SAMPLE_WIDTH, SAMPLE_HEIGHT);
            opt.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(mPath, opt);
        }

        private int calculateInSampleSize(int currWidth, int currHeight, int reqWidth, int reqHeight) {
            int inSampleSize = 1;

            if (currHeight > reqHeight || currWidth > reqWidth) {

                final int halfHeight = currHeight / 2;
                final int halfWidth = currWidth / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            if(bitmap != null && cardViewReference != null){
                mImage = bitmap;
                cardViewReference.setImageDrawable(new BitmapDrawable(Resources.getSystem(), mImage));
                cardViewReference = null;
            }
        }
    }

}
