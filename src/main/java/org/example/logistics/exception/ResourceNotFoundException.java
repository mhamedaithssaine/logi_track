package org.example.logistics.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResourceNotFoundException withId(String resourceName, Long id) {
        return new ResourceNotFoundException(
                resourceName + " non trouvé avec l'ID: " + id
        );
    }

    public static ResourceNotFoundException withEmail(String resourceName, String email) {
        return new ResourceNotFoundException(
                resourceName + " non trouvé avec l'email: " + email
        );
    }

    public static ResourceNotFoundException withString(String resourceName, String fieldName, String value) {
        return new ResourceNotFoundException(
                resourceName + " avec " + fieldName + " : " + value
        );
    }
}