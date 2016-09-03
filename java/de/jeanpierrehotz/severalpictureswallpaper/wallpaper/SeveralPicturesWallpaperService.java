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

package de.jeanpierrehotz.severalpictureswallpaper.wallpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.ArrayList;

import de.jeanpierrehotz.severalpictureswallpaper.R;
import de.jeanpierrehotz.severalpictureswallpaper.utils.DoubleSwipeGestureDetector;
import de.jeanpierrehotz.severalpictureswallpaper.wallpaper.data.WallpaperImage;

/**
 *
 */
public class SeveralPicturesWallpaperService extends WallpaperService{

    private final Handler handler = new Handler();

    @Override
    public Engine onCreateEngine(){
        return new SeveralPicturesWallpaper();
    }

    private class SeveralPicturesWallpaper extends Engine{

        private static final int DEBUG_ALPHASTEPS = 15;
        private static final int DEBUG_ALPHATIME = 1;
        private static final int TIME_STRETCH = 1000;

        private final Paint             p;
        private final Runnable          run;

        private float                   x, y;
        private boolean                 visible;

        private int                     wallpaperindex;

        private boolean                 detectGestures;
        private int                     showPictureTime;

        private int                     alpha;
        private boolean                 fading;

        private int                     previousImage;
        private int                     currentImage;
        private int                     nextImage;

        private FadeDirection           fadeDirection;

        private ArrayList<WallpaperImage> images;

        private DoubleSwipeGestureDetector mDoubleLeftSwipeGestureDetector;
        private DoubleSwipeGestureDetector.OnDoubleLeftSwipeListener mOnDoubleLeftSwipeListener = new DoubleSwipeGestureDetector.OnDoubleLeftSwipeListener(){
            @Override
            public void onDoubleLeftSwiped(){
                if(!fading){
                    fadeDirection = FadeDirection.forward;
                    handler.removeCallbacks(run);
                    handler.post(run);
                }
            }
        };
        private DoubleSwipeGestureDetector.OnDoubleRightSwipeListener mOnDoubleRightSwipeListener = new DoubleSwipeGestureDetector.OnDoubleRightSwipeListener(){
            @Override
            public void onDoubleRightSwiped(){
                if(!fading){
                    fadeDirection = FadeDirection.backward;
                    handler.removeCallbacks(run);
                    handler.post(run);
                }
            }
        };

        private SeveralPicturesWallpaper(){
            p = new Paint();

            loadPreferences();

            mDoubleLeftSwipeGestureDetector = new DoubleSwipeGestureDetector();
            mDoubleLeftSwipeGestureDetector.addOnDoubleLeftSwipeListener(mOnDoubleLeftSwipeListener);
            mDoubleLeftSwipeGestureDetector.addOnDoubleRightSwipeListener(mOnDoubleRightSwipeListener);

            run = new Runnable(){
                @Override
                public void run(){
                    if(!fading){
                        fading = true;
                        alpha = 255;
                    }
                    draw();
                }
            };
        }

        private void loadPreferences(){
            wallpaperindex = getSharedPreferences(getString(R.string.preferencecode_wallpapersinfo), MODE_PRIVATE).getInt(getString(R.string.prefs_wallpaperindex), 0);

            images = WallpaperImage.loadFromSharedPreferences(getSharedPreferences(getString(R.string.preferencecode_wallpaperimages) + wallpaperindex, MODE_PRIVATE));

            if(images.size() > 0){
                SharedPreferences prefs = getSharedPreferences(getString(R.string.preferencecode_miscellanous) + wallpaperindex, MODE_PRIVATE);

                detectGestures = prefs.getBoolean(getString(R.string.prefs_detectGestures), true);

                showPictureTime = prefs.getInt(getString(R.string.prefs_showPictureTime), 10);
                currentImage = prefs.getInt(getString(R.string.prefs_currentIndex), 0) % images.size();

                currentImage = (currentImage == 0)? images.size() - 1: currentImage - 1;

                fadeDirection = FadeDirection.forward;

                fading = true;
                alpha = 0;

                refreshImages();
            }
        }

        private void refreshImages(){
            previousImage = (currentImage == 0)? images.size() - 1: currentImage - 1;
            nextImage = (currentImage == images.size() - 1)? 0: currentImage + 1;

            for(int i = 0; i < images.size(); i++){
                if(i == previousImage || i == currentImage || i == nextImage){
                    images.get(i).loadImage();
                }else{
                    images.get(i).releaseImage();
                }
            }
        }

        private void draw(){
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try{
                c = holder.lockCanvas();
                if(c != null){
                    if(images.size() > 0){
                        if(!fading){
                            images.get(currentImage).drawImage(x, y, c, p);
                        }else{
                            if(fadeDirection == FadeDirection.forward){
                                p.setAlpha(alpha);
                                images.get(currentImage).drawImage(x, y, c, p);
                                p.setAlpha(255 - alpha);
                                images.get(nextImage).drawImage(x, y, c, p);
                            }else{
                                p.setAlpha(alpha);
                                images.get(currentImage).drawImage(x, y, c, p);
                                p.setAlpha(255 - alpha);
                                images.get(previousImage).drawImage(x, y, c, p);
                            }
                            refreshAlphas();
                        }
                    }else{
                        p.setColor(Color.BLACK);
                        p.setStyle(Paint.Style.FILL);
                        c.drawRect(0, 0, x, y, p);
                    }
                }
            }finally{
                if(c != null){
                    holder.unlockCanvasAndPost(c);
                }
            }

            handler.removeCallbacks(run);

            if(visible && !fading){
                handler.postDelayed(run, showPictureTime * TIME_STRETCH);
            }else if(visible){
                handler.postDelayed(run, DEBUG_ALPHATIME);
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event){
            super.onTouchEvent(event);

            if(detectGestures)
                mDoubleLeftSwipeGestureDetector.onTouchEvent(event);
        }

        private void refreshAlphas(){
            alpha -= DEBUG_ALPHASTEPS;

            if(alpha <= 0){
                if(fadeDirection == FadeDirection.forward){
                    currentImage = ++currentImage % images.size();
                }else{
                    currentImage = (currentImage == 0)? images.size() - 1: currentImage - 1;
                }

                fading = false;
                fadeDirection = FadeDirection.forward;

                refreshImages();
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder){
            super.onCreate(surfaceHolder);

            loadPreferences();
        }

        @Override
        public void onDestroy(){
            super.onDestroy();

            getSharedPreferences(getString(R.string.preferencecode_miscellanous) + wallpaperindex, MODE_PRIVATE).edit().putInt(getString(R.string.prefs_currentIndex), currentImage).apply();
            handler.removeCallbacks(run);
        }

        @Override
        public void onVisibilityChanged(boolean v){
            if(visible = v){
                loadPreferences();
                handler.post(run);
            }else{
                getSharedPreferences(getString(R.string.preferencecode_miscellanous) + wallpaperindex, MODE_PRIVATE).edit().putInt(getString(R.string.prefs_currentIndex), currentImage).apply();
                handler.removeCallbacks(run);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height){
            super.onSurfaceChanged(holder, format, width, height);

            if(width < height){
                x = width;
                y = height;
            }

            handler.post(run);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder){
            super.onSurfaceDestroyed(holder);

            visible = false;

            getSharedPreferences(getString(R.string.preferencecode_miscellanous) + wallpaperindex, MODE_PRIVATE).edit().putInt(getString(R.string.prefs_currentIndex), currentImage).apply();
            handler.removeCallbacks(run);
        }
    }

    enum FadeDirection{
        forward,
        backward
    }
}
