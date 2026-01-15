package com.hth.udecareer.service;

import com.hth.udecareer.entities.QrTransaction;

public interface QrPaymentGrantService {
    void processQrPaymentGrant(QrTransaction tx, String usedBy);
}
