package com.badlogic.gdx.video.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.badlogic.gdx.video.VideoPlayerInitException;

import java.io.IOException;

public class VideoPlayerWidget extends Widget {
    private static final String TAG = VideoPlayerWidget.class.getSimpleName();
    private static final Color tmpColor = new Color();

    private VideoPlayer videoPlayer;
    private FileHandle videoFile;

    private boolean initialized = false;
    private boolean repeat = false;
    private boolean playOnPrepared = true;
//    private boolean resizePending = false;

    private final VideoPlayer.CompletionListener completionListener = new VideoPlayer.CompletionListener() {
        @Override
        public void onCompletionListener(VideoPlayer videoPlayer) {
            VideoCompletionEvent changeEvent = Pools.obtain(VideoCompletionEvent.class);
            changeEvent.initialize(videoFile);
            fire(changeEvent);
            Pools.free(changeEvent);
        }
    };

    public VideoPlayerWidget() {
    }

    public VideoPlayerWidget(FileHandle videoFile) {
        this.videoFile = videoFile;
    }

    public void setVideoFile(FileHandle videoFile) {
        if (this.videoFile != null && this.videoFile.equals(videoFile)) return;

        this.videoFile = videoFile;

        playVideoInternal(videoFile);
    }

    /**
     * The {@link com.badlogic.gdx.video.VideoPlayer} instance gets available once the actor is added to a stage.
     * @return local {@link com.badlogic.gdx.video.VideoPlayer} instance with the provided video file initialized.
     */
    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
        if (videoPlayer != null) {
            videoPlayer.setRepeat(repeat);
        }
    }

    public boolean isPlayOnPrepared() {
        return playOnPrepared;
    }

    public void setPlayOnPrepared(boolean playOnPrepared) {
        this.playOnPrepared = playOnPrepared;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.validate();
        batch.end();

        Color col = getColor();
        videoPlayer.setColor(tmpColor.set(col.r, col.g, col.b, col.a * parentAlpha));

        videoPlayer.setProjectionMatrix(batch.getProjectionMatrix());
        videoPlayer.render(getX(), getY(), getWidth(), getHeight());

        batch.begin();
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);

        if (stage != null) {
            initialize();
        } else {
            reset();
        }
    }

    @Override
    public float getMinWidth() {
        return 0;
    }

    @Override
    public float getMinHeight() {
        return 0;
    }

    @Override
    public float getPrefWidth() {
        if (initialized) {
            return videoPlayer.getVideoWidth();
        }
        return super.getPrefWidth();
    }

    @Override
    public float getPrefHeight() {
        if (initialized) {
            return videoPlayer.getVideoHeight();
        }
        return super.getPrefHeight();
    }

    protected void initialize() {
        if (initialized) return;

//        performPendingResize();

        try {
            videoPlayer = VideoPlayerCreator.createVideoPlayer();
            videoPlayer.setRepeat(repeat);
            videoPlayer.setPreparedListener(new VideoPlayer.VideoPreparedListener() {
                @Override
                public void onVideoPrepared(VideoPlayer videoPlayer, float width, float height) {
                    invalidateHierarchy();
                    // Start playback straight away.
                    if (playOnPrepared && videoPlayer != null) {
                        videoPlayer.play();
                    }
                }
            });
            videoPlayer.setOnCompletionListener(completionListener);
            videoPlayer.prepare(videoFile);
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error initializing video player.", e);
            if (videoPlayer != null) {
                videoPlayer.dispose();
                videoPlayer = null;
            }
            return;
        }

//        resizePending = false;
        initialized = true;

//        if (videoFile != null) {
//            playVideoInternal(videoFile);
//        }
    }

    protected void reset() {
        if (!initialized) return;

        videoPlayer.dispose();
        videoPlayer = null;

//        resizePending = false;
        initialized = false;
    }

    protected void playVideoInternal(FileHandle videoFile) {
        if (initialized && videoPlayer.isPrepared()) {
            videoPlayer.play();
        }
    }

//    private void performPendingResize() {
//        if (!resizePending) return;
//
//        final int width;
//        final int height;
//
//        // Size may be zero if the widget wasn't laid out yet.
//        if ((int)getWidth() == 0 || (int)getHeight() == 0) {
//            // If the size of the widget is not defined,
//            // just resize to a small buffer to keep the memory footprint low.
//            width = 16;
//            height = 16;
//
//        } else {
//
//        }
//
//        resizePending = false;
//    }

}