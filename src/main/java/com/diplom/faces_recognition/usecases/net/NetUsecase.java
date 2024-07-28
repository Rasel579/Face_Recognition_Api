package com.diplom.faces_recognition.usecases.net;

import com.diplom.faces_recognition.entity.netmodel.INetFrame;
import com.diplom.faces_recognition.models.GenericResponse;

public interface NetUsecase {
    GenericResponse predictObjets(INetFrame frame);
}
