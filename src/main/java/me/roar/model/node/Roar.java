package me.roar.model.node;

import com.google.gson.Gson;
import ligo.meta.*;

import java.util.Date;

/**
 * Stores details about the roaring.
 */
@Entity(type = EntityType.NODE, label = "roar")
public class Roar {
  private Long id;
  private String text;
  private boolean isDirect = false;
  private long uAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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

  @Indexed(type = IndexType.FULL_TEXT, name = "roar_text_ft")
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Roar withText(String text) {
    setText(text);
    return this;
  }

  public boolean isDirect() {
    return isDirect;
  }

  public void setDirect(boolean isDirect) {
    this.isDirect = isDirect;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
}
