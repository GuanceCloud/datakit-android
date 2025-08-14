package com.ft.sdk.sessionreplay.internal.persistence;

/**
 * Interface for encryption and decryption operations.
 */
public interface Encryption {

    /**
     * Encrypts the given byte array with the user-chosen encryption method.
     * 
     * @param data Bytes to encrypt.
     * @return Encrypted byte array.
     */
    byte[] encrypt(byte[] data);

    /**
     * Decrypts the given byte array with the user-chosen encryption method.
     * 
     * @param data Bytes to decrypt. Be aware that data to decrypt could be encrypted in a previous
     *             app launch, so the implementation should handle cases where decryption might fail
     *             (e.g., if the key used for encryption is different from the key used for decryption).
     * @return Decrypted byte array.
     */
    byte[] decrypt(byte[] data);
}
