package me.roar.fixture;

import ligo.config.DBConfig;

/**
 * Sets up DB
 */
public class BaseIntegrationTest {

  protected DBConfig dbConfig = new DBConfig("src/test/resources/db/integ.db");

}
