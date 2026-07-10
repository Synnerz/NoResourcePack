package com.github.synnerz.noresourcepack

import com.google.common.collect.ImmutableMultimap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.jvm.optionals.getOrNull

object ItemsData {
    /**
     * Thanks a lot to <https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO>
     * as this data set was built with it
     */
    val itemIds = Gson().fromJson(
        this::class.java.getResourceAsStream("/assets/noresourcepack/ItemDataSet.json")
            ?.bufferedReader()
            .use { it?.readText() },
        object : TypeToken<Map</* sbId */String, Map</* model/texture(value) */String, /* value */String>>>() {}
    )
    val cachedItems = mutableMapOf<String, GameProfile>()

    fun skyblockId(itemStack: ItemStack): String? {
        val extraAttributes = itemStack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return null
        val sbId = extraAttributes.getString("id").getOrNull()
        val isQuiver = extraAttributes.getString("quiver_arrow").isPresent
        return if (isQuiver && sbId == null) "NRP\$QUIVER" else sbId
    }

    fun modelId(itemStack: ItemStack): String? {
        val id = skyblockId(itemStack) ?: return null
        return itemIds[id]?.get("model")
    }

    fun gameProfile(sbId: String): GameProfile? {
        if (NoResourcePack.whitelistedItems.contains(sbId)) return null
        return cachedItems[sbId]
    }

    fun gameProfile(itemStack: ItemStack): GameProfile?
        = skyblockId(itemStack)?.let { gameProfile(it) }

    fun fromModelId(itemStack: ItemStack, modelId: Identifier?): Identifier? {
        if (modelId == null) return null
        if (!modelId.namespace.startsWith("hypixel_skyblock")) return modelId
        val sbId = skyblockId(itemStack) ?: return modelId
        if (NoResourcePack.whitelistedItems.contains(sbId)) return modelId

        val cache = itemIds[sbId]
        if (cache != null) {
            val id = modelId(itemStack) ?: return modelId
            if (id == "minecraft:player_head") {
                cache["value"]?.let {
                    if (cachedItems.containsKey(sbId)) return@let

                    cachedItems[sbId] = GameProfile(
                        UUID.randomUUID(),
                        "nrp\$fakeItem",
                        PropertyMap(ImmutableMultimap.of(
                            "textures",
                            Property(
                                "textures",
                                it
                            )
                        ))
                    )
                }
            }
            return Identifier.parse(id)
        }

        return BuiltInRegistries.ITEM.getKey(itemStack.item)
    }
}