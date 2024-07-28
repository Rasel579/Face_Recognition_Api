package com.diplom.faces_recognition.usecases.net;

import com.diplom.faces_recognition.entity.netmodel.INetFrame;
import com.diplom.faces_recognition.models.GenericResponse;
import com.diplom.faces_recognition.nets.yolo.contract.AbstractObjDetectionNet;
import com.diplom.faces_recognition.presenters.net.NetPresenter;
import org.springframework.beans.factory.annotation.Autowired;

public class NetIteractor implements NetUsecase {
    @Autowired
    private NetPresenter presenter;
    @Autowired
    private AbstractObjDetectionNet netSource;

    @Override
    public GenericResponse predictObjets(INetFrame frame) {
        GenericResponse response = netSource.feedNet(frame);

        if (response.getErrorCode() == null) {
            return presenter.prepareSuccessView(response);
        }

        return presenter.prepareFailureView(response);

    }
}
