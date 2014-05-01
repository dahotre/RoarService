package ligo.meta;

import java.util.Date;

/**
 * Base class that will always maintain createdAt, updatedAt, and Id. Dates are stored in form of
 * long, and helpers are provided to get util date format.
 * 
 */
public abstract class LigoEntity {
  private Long id;
  private long cAt;
  private long uAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public long getcAt() {
    return cAt;
  }

  public void setcAt(long cAt) {
    this.cAt = cAt;
  }

  public long getuAt() {
    return uAt;
  }

  public void setuAt(long uAt) {
    this.uAt = uAt;
  }

  public Date getCreatedAt() {
    return new Date(this.cAt);
  }
  public void setCreatedAt(Date dt) {
    this.cAt = dt.getTime();
  }
  public Date getUpdatedAt() {
    return new Date(this.uAt);
  }
  public void setUpdatedAt(Date dt) {
    this.uAt = dt.getTime();
  }
}
