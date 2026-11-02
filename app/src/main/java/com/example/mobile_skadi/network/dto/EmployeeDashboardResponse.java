package com.example.mobile_skadi.network.dto;

import java.util.List;

public class EmployeeDashboardResponse {
    private Long userId;
    private String userName;
    private String cdName;
    private int atribuidosCount;
    private int criticosCount;
    private int emAtendimentoCount;
    private int slaVencidoCount;
    private int proximosVencimentoCount;
    private PriorityAlertDto nextAction;
    private List<PriorityAlertDto> queue;

    public EmployeeDashboardResponse() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCdName() {
        return cdName;
    }

    public void setCdName(String cdName) {
        this.cdName = cdName;
    }

    public int getAtribuidosCount() {
        return atribuidosCount;
    }

    public void setAtribuidosCount(int atribuidosCount) {
        this.atribuidosCount = atribuidosCount;
    }

    public int getCriticosCount() {
        return criticosCount;
    }

    public void setCriticosCount(int criticosCount) {
        this.criticosCount = criticosCount;
    }

    public int getEmAtendimentoCount() {
        return emAtendimentoCount;
    }

    public void setEmAtendimentoCount(int emAtendimentoCount) {
        this.emAtendimentoCount = emAtendimentoCount;
    }

    public int getSlaVencidoCount() {
        return slaVencidoCount;
    }

    public void setSlaVencidoCount(int slaVencidoCount) {
        this.slaVencidoCount = slaVencidoCount;
    }

    public int getProximosVencimentoCount() {
        return proximosVencimentoCount;
    }

    public void setProximosVencimentoCount(int proximosVencimentoCount) {
        this.proximosVencimentoCount = proximosVencimentoCount;
    }

    public PriorityAlertDto getNextAction() {
        return nextAction;
    }

    public void setNextAction(PriorityAlertDto nextAction) {
        this.nextAction = nextAction;
    }

    public List<PriorityAlertDto> getQueue() {
        return queue;
    }

    public void setQueue(List<PriorityAlertDto> queue) {
        this.queue = queue;
    }
}
