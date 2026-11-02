package com.example.mobile_skadi.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatResponse {
    private String resposta;

    @SerializedName("agentes_chamados")
    private List<String> agentesChamados;

    public ChatResponse() {
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public List<String> getAgentesChamados() {
        return agentesChamados;
    }

    public void setAgentesChamados(List<String> agentesChamados) {
        this.agentesChamados = agentesChamados;
    }
}
