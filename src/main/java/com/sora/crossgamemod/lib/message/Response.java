package com.sora.crossgamemod.lib.message;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public final class Response {
    @SerializedName("IsSuccess")
    public boolean isSuccess;
    @SerializedName("ErrorMessage")
    @Nullable
    public String errorMessage;
    public Response(boolean isSuccess, String errorMessage) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }
}
