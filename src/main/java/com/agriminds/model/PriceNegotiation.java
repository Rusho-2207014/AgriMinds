package com.agriminds.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PriceNegotiation {
    private Long id;
    private Long farmerCropId;
    private Long buyerId;
    private String buyerName;
    private BigDecimal offeredPrice;
    private BigDecimal farmerAskingPrice;
    private BigDecimal quantityKg;
    private String status; // Pending, Accepted, Rejected, Counter
    private LocalDateTime negotiationDate;
    private LocalDateTime acceptedDate;
    private String notes;

    public PriceNegotiation() {
    }

    public PriceNegotiation(Long farmerCropId, Long buyerId, BigDecimal offeredPrice) {
        this.farmerCropId = farmerCropId;
        this.buyerId = buyerId;
        this.offeredPrice = offeredPrice;
        this.status = "Pending";
        this.negotiationDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFarmerCropId() {
        return farmerCropId;
    }

    public void setFarmerCropId(Long farmerCropId) {
        this.farmerCropId = farmerCropId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public BigDecimal getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(BigDecimal offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public BigDecimal getFarmerAskingPrice() {
        return farmerAskingPrice;
    }

    public void setFarmerAskingPrice(BigDecimal farmerAskingPrice) {
        this.farmerAskingPrice = farmerAskingPrice;
    }

    public BigDecimal getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(BigDecimal quantityKg) {
        this.quantityKg = quantityKg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getNegotiationDate() {
        return negotiationDate;
    }

    public void setNegotiationDate(LocalDateTime negotiationDate) {
        this.negotiationDate = negotiationDate;
    }

    public LocalDateTime getAcceptedDate() {
        return acceptedDate;
    }

    public void setAcceptedDate(LocalDateTime acceptedDate) {
        this.acceptedDate = acceptedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "PriceNegotiation{" +
                "id=" + id +
                ", farmerCropId=" + farmerCropId +
                ", buyerId=" + buyerId +
                ", buyerName='" + buyerName + '\'' +
                ", offeredPrice=" + offeredPrice +
                ", farmerAskingPrice=" + farmerAskingPrice +
                ", quantityKg=" + quantityKg +
                ", status='" + status + '\'' +
                ", negotiationDate=" + negotiationDate +
                ", acceptedDate=" + acceptedDate +
                '}';
    }
}
