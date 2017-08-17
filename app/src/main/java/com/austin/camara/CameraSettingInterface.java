package com.austin.camara;

/**
 * Created by gy on 2017/8/10.
 */

public interface CameraSettingInterface {
    void onCameraInavailable();

    int[] onGetProposalPreviewSize();

    void onDoneRecording(String filePath);

    /**
     * int[0] min
     * int[1] max
     * @return
     */
    int[] getTimeLimit();
}