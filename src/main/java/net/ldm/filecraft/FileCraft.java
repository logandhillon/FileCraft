package net.ldm.filecraft;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.ldm.filecraft.command.SshCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCraft implements ModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("FileCraft");

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SshCommand.register(dispatcher));
		LOG.info("Initialization complete");
	}
}