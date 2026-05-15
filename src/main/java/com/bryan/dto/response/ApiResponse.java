package com.bryan.dto.response;

public record ApiResponse<T>(
        boolean success,
        int statusCode,
        T data,
        String message
) {
    // Trả về thành công với status code mặc định là 200
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, data, "Success");
    }

    // Trả về thành công với data và message (200)
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, 200, data, message);
    }

    // Trả về thành công với status code tùy chỉnh (VD: 201 Created)
    public static <T> ApiResponse<T> success(int statusCode, T data) {
        return new ApiResponse<>(true, statusCode, data, "Success");
    }

    // Trả về thành công với status code tùy chỉnh và message
    public static <T> ApiResponse<T> success(int statusCode, T data, String message) {
        return new ApiResponse<>(true, statusCode, data, message);
    }

    // Trả về lỗi với status code tùy chỉnh (VD: 400, 404, 500)
    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return new ApiResponse<>(false, statusCode, null, message);
    }

    // Trả về lỗi với status code mặc định (VD: 500 hoặc 400 tùy logic dự án của bạn)
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, 500, null, message);
    }
}
