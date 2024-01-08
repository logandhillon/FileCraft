package net.ldm.filecraft;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCraft implements ModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("FileCraft");

	@Override
	public void onInitialize() {
		LOG.info("Hello world!");
	}
}