package org.p2p.solanaj.core;

import java.nio.ByteBuffer;
import java.util.*;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ShortvecEncoding;
import org.p2p.solanaj.utils.TweetNaclFast;

/**
 * Represents a Solana transaction.
 * This class allows for building, signing, and serializing transactions.
 */
public class Transaction {

    public static final int SIGNATURE_LENGTH = 64;

    private final Message message;
    private final List<byte[]> signatures;
    private byte[] serializedMessage;

    /**
     * Constructs a new Transaction instance.
     */
    public Transaction() {
        this.message = new Message();
        this.signatures = new ArrayList<>(); // Use diamond operator
    }

    /**
     * Adds an instruction to the transaction.
     *
     * @param instruction The instruction to add
     * @return This Transaction instance for method chaining
     * @throws NullPointerException if the instruction is null
     */
    public Transaction addInstruction(TransactionInstruction instruction) {
        Objects.requireNonNull(instruction, "Instruction cannot be null"); // Add input validation
        message.addInstruction(instruction);
        return this;
    }

    /**
     * Sets the recent blockhash for the transaction.
     *
     * @param recentBlockhash The recent blockhash to set
     * @throws NullPointerException if the recentBlockhash is null
     */
    public void setRecentBlockHash(String recentBlockhash) {
        Objects.requireNonNull(recentBlockhash, "Recent blockhash cannot be null"); // Add input validation
        message.setRecentBlockHash(recentBlockhash);
    }


    public Transaction setFeePayer(Account feePayer) {
        this.message.setFeePayer(feePayer);
        return this;
    }

    /**
     * Signs the transaction with a single signer.
     *
     * @param signer The account to sign the transaction
     * @throws NullPointerException if the signer is null
     */
    public void sign(Account signer) {
        sign(Arrays.asList(Objects.requireNonNull(signer, "Signer cannot be null"))); // Add input validation
    }

    /**
     * Signs the transaction with multiple signers.
     *
     * @param signers The list of accounts to sign the transaction
     * @throws IllegalArgumentException if no signers are provided
     */
    public void sign(List<Account> signers) {
        if (signers == null || signers.isEmpty()) {
            throw new IllegalArgumentException("No signers provided");
        }

        Account feePayer = message.getFeePayer();
        if ( feePayer == null ) {
            // if fee payer not set, then pick the first account from signers as fee payer
            feePayer = signers.get(0);
            message.setFeePayer(feePayer);
        } else {
            // if fee payer is set, confirm that it is included as a signer
            if ( !signers.contains(feePayer) )
                throw new AccountNotFound("feePayer not included in signers list");
        }

        serializedMessage = message.serialize();

        for (Account signer : signers) {
            try {
                TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getSecretKey());
                byte[] signature = signatureProvider.detached(serializedMessage);
                //signatures.add(Base58.encode(signature));
                signatures.add(signature);
            } catch (Exception e) {
                throw new RuntimeException("Error signing transaction", e); // Improve exception handling
            }
        }
    }

    /**
     * Serializes the transaction into a byte array.
     *
     * @return The serialized transaction as a byte array
     */
    public byte[] serialize() {
        int signaturesSize = signatures.size();
        byte[] signaturesLength = ShortvecEncoding.encodeLength(signaturesSize);

        // Calculate total size before allocating ByteBuffer
        int totalSize = signaturesLength.length + signaturesSize * SIGNATURE_LENGTH + serializedMessage.length;
        ByteBuffer out = ByteBuffer.allocate(totalSize);

        out.put(signaturesLength);

        for (byte[] signature : signatures) {
            //byte[] rawSignature = Base58.decode(signature);
            //out.put(rawSignature);
            out.put(signature);
        }

        out.put(serializedMessage);

        return out.array();
    }

    protected Message getMessage() {
        return message;
    }

    protected List<byte[]> getSignatures() {
        return signatures;
    }

    protected byte[] getSerializedMessage() {
        return serializedMessage;
    }
}
