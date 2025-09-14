// path: data/src/main/java/com/securitybusinesssuite/data/repository/SequenceRepository.java
package com.securitybusinesssuite.data.repository;

import com.securitybusinesssuite.data.entity.*;
import java.util.Optional;

public interface SequenceRepository {
    // NCF Sequences
    Optional<NCFSequence> findNCFSequenceByTypeAndYear(Invoice.NCFType type, int year);
    NCFSequence saveNCFSequence(NCFSequence sequence);
    NCFSequence updateNCFSequence(NCFSequence sequence);

    // Client Sequences
    Optional<ClientSequence> findClientSequenceByYear(int year);
    ClientSequence saveClientSequence(ClientSequence sequence);
    ClientSequence updateClientSequence(ClientSequence sequence);

    // Invoice Sequences
    Optional<InvoiceSequence> findInvoiceSequenceByYear(int year);
    InvoiceSequence saveInvoiceSequence(InvoiceSequence sequence);
    InvoiceSequence updateInvoiceSequence(InvoiceSequence sequence);

    // Receipt Sequences
    Optional<ReceiptSequence> findReceiptSequenceByYear(int year);
    ReceiptSequence saveReceiptSequence(ReceiptSequence sequence);
    ReceiptSequence updateReceiptSequence(ReceiptSequence sequence);
}