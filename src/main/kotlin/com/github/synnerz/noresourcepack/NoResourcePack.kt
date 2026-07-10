package com.github.synnerz.noresourcepack

import com.github.synnerz.noresourcepack.mixin.AbstractContainerScreenAccessor
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files

object NoResourcePack : ModInitializer {
	const val MOD_ID: String = "noresourcepack"
	private val LOGGER = LoggerFactory.getLogger(MOD_ID)
	private val mcRoot = File("./config")
	private val configFile = File(mcRoot, "nrp/config.json").apply {
		if (!exists()) {
			Files.createDirectories(parentFile.toPath())
			Files.createFile(toPath())
		}
	}
	private val oldConfigFile = File(mcRoot, "nrpconfig.json").apply {
		if (!exists()) {
			Files.createDirectories(parentFile.toPath())
			Files.createFile(toPath())
		}
	}
	private val oldConfigFile2 = File(mcRoot, "nrptooltipconfig.json").apply {
		if (!exists()) {
			Files.createDirectories(parentFile.toPath())
			Files.createFile(toPath())
		}
	}
	private val gson = Gson()
	val keybindCategory by lazy {
		KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(
				"noresourcepack",
				"keybinds"
			)
		)
	}
	val whitelistKeybind = KeyMappingHelper.registerKeyMapping(KeyMapping(
		"key.noresourcepack.whitelist",
		GLFW.GLFW_KEY_UNKNOWN,
		keybindCategory
	))
	val whitelistedItems = mutableSetOf<String>()
	var vanillaTooltip = true
	var blacklistMode = false

	override fun onInitialize() {
		LOGGER.info("Intialized Synnerz/$MOD_ID")
		ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
			dispatcher.register(
				ClientCommands.literal("nrp")
					.executes { 1 }
					.then(ClientCommands.literal("tooltip").executes { ctx ->
						vanillaTooltip = !vanillaTooltip
						ctx.source.sendFeedback(Component.literal("vanilla tooltip $vanillaTooltip"))
						1
					})
					.then(ClientCommands.literal("whitelist")
						.then(
							ClientCommands.argument("skyblockId", StringArgumentType.string())
								.executes { ctx ->
									val sbId = StringArgumentType.getString(ctx, "skyblockId").uppercase()
									var added = false
									if (whitelistedItems.contains(sbId))
										whitelistedItems.remove(sbId)
									else
										added = whitelistedItems.add(sbId)
									ctx.source.sendFeedback(
										Component
											.literal("[NRP] ")
											.withStyle(Style.EMPTY.withColor(ChatFormatting.RED))
											.append(Component
												.literal(if (blacklistMode) "Blacklist " else "Whitelist ")
												.withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))
											)
											.append(Component
												.literal(if (added) "added " else "removed ")
												.withStyle(Style.EMPTY.withColor(if (added) ChatFormatting.GREEN else ChatFormatting.RED))
											)
											.append(Component
												.literal(sbId)
												.withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))
											)
									)
									1
								}
						)
					)
					.then(ClientCommands.literal("blacklistMode").executes { ctx ->
						blacklistMode = !blacklistMode
						ctx.source.sendFeedback(Component.literal("blacklisted mode $blacklistMode"))
						1
					})
			)
		}

		ScreenEvents.BEFORE_INIT.register { minecraft, screen, i, i1 ->
			ScreenKeyboardEvents.allowKeyRelease(screen).register { screen, event ->
				if (whitelistKeybind.matches(event)) {
					val itemStack = (screen as? AbstractContainerScreenAccessor)?.hoveredSlot?.item
					itemStack?.let { stack ->
						ItemsData.skyblockId(stack)?.let { sbId ->
							var added = false
							if (whitelistedItems.contains(sbId))
								whitelistedItems.remove(sbId)
							else
								added = whitelistedItems.add(sbId)

							minecraft.player!!.sendSystemMessage(
								Component
									.literal("[NRP] ")
									.withStyle(Style.EMPTY.withColor(ChatFormatting.RED))
									.append(Component
										.literal(if (blacklistMode) "Blacklist " else "Whitelist ")
										.withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))
									)
									.append(Component
										.literal(if (added) "added " else "removed ")
										.withStyle(Style.EMPTY.withColor(if (added) ChatFormatting.GREEN else ChatFormatting.RED))
									)
									.append(Component
										.literal(sbId)
										.withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))
									)
							)
						}
					}
				}
				true
			}
		}

		// load config
		val oldWhitelist = mutableSetOf<String>()
		var oldVanillaTooltip = false

		gson.fromJson(oldConfigFile.readText(), object : TypeToken<Set<String>>() {})?.let {
			oldWhitelist.addAll(it)
		}
		gson.fromJson(oldConfigFile2.readText(), Boolean::class.java)?.let {
			oldVanillaTooltip = it
		}
		gson.fromJson(configFile.readText(), object : TypeToken<Map<String, Any>>() {}).let { map ->
            val tooltip = map?.get("vanillaTooltip") as? Boolean ?: oldVanillaTooltip
			val list = (map?.get("whitelist") as? ArrayList<String>)?.toSet() ?: oldWhitelist
			val bl = map?.get("blacklistMode") as? Boolean ?: false

			whitelistedItems.addAll(list)
			vanillaTooltip = tooltip
			blacklistMode = bl
		}

		// save config
		ClientLifecycleEvents.CLIENT_STOPPING.register {
			val obj = JsonObject()

			obj.addProperty("vanillaTooltip", vanillaTooltip)
			obj.addProperty("blacklistMode", blacklistMode)
			obj.add("whitelist", JsonArray().apply {
				whitelistedItems.forEach { add(it) }
			})

			configFile.writeText(gson.toJson(obj))
		}
	}
}
