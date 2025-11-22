package com.tu2l.pdf.api.models.base;

/**
 * Marker interface for all request models.
 * <p>
 * This interface is used to indicate that a class is a request model within the API.
 * It does not define any methods or behavior, but allows for type safety, generic processing,
 * and future extensibility (e.g., for validation, serialization, or request handling).
 * Implementing this interface helps to group all request models and enables framework-level
 * operations that can target all requests uniformly.
 */
public interface BaseRequest {
    
}
