package com.anoop.videoStream.dto;

public class TelegramResponse {

    private boolean ok;

    private Result result;

    @Override
    public String toString() {
        return "TelegramResponse [ok=" + ok + ", result=" + result + "]";
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}