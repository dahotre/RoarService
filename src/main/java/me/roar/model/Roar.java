package me.roar.model;

import ligo.meta.*;

import java.util.Date;

/**
 * Stores details about the roaring.
 */
@Entity(type = EntityType.NODE, label = "roar")
public class Roar {
  private long id;
  private String text;
  private boolean isDirect;
  private long uAt;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getuAt() {
    return uAt;
  }

  public void setuAt(long uAt) {
    this.uAt = uAt;
  }

  @Transient
  public Date getUpdatedAt() {
    return new Date(uAt);
  }

  public void setUpdatedAt(Date date) {
    this.uAt = date.getTime();
  }

  @Index(type = IndexType.FULL_TEXT, name = "roar_text_ft")
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public boolean isDirect() {
    return isDirect;
  }

  public void setDirect(boolean isDirect) {
    this.isDirect = isDirect;
  }
}
