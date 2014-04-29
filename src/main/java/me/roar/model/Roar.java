package me.roar.model;

import java.util.Date;

/**
 * TODO : Purpose
 */
public class Roar {
  private String text;
  private Date roaredAt;
  private boolean isDirect;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Date getRoaredAt() {
    return roaredAt;
  }

  public void setRoaredAt(Date roaredAt) {
    this.roaredAt = roaredAt;
  }

  public boolean isDirect() {
    return isDirect;
  }

  public void setDirect(boolean isDirect) {
    this.isDirect = isDirect;
  }
}
