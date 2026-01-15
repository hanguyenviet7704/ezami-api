package com.hth.udecareer.model.qr;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Bank profile contains configuration that may differ between banks for MAI (merchant account info) format
 * For now we allow configuring whether the bank info should be nested and the GUID to use.
 */
@Getter
@AllArgsConstructor
public class BankProfile {
    // Guid for MAI (EMVCo). Default A000000727 is VietQR.
    private final String guid;
    // Should use nested bank info (00=bank id + 01=account) vs a flat 01=account only
    private final boolean nestedBankInfo;
}
