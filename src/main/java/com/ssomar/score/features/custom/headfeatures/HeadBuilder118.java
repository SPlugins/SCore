package com.ssomar.score.features.custom.headfeatures;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeadBuilder118 {

    private static final UUID RANDOM_UUID = UUID.fromString("b33183ad-e9c0-4d48-8eea-f8c9358d3568"); // We reuse the same "random" UUID all the time
    private static PlayerProfile getProfile(String url) {
        PlayerProfile profile = Bukkit.createPlayerProfile(RANDOM_UUID); // Get a new player profile
        PlayerTextures textures = profile.getTextures();
        URL urlObject;
        try {
            urlObject = new URL(url); // The URL to the skin, for example: https://textures.minecraft.net/texture/18813764b2abc94ec3c3bc67b9147c21be850cdf996679703157f4555997ea63a
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Invalid URL", exception);
        }
        textures.setSkin(urlObject); // Set the skin of the player profile to the URL
        profile.setTextures(textures); // Set the textures back to the profile
        return profile;
    }

    public static ItemStack getHead(String url) {
        PlayerProfile profile = getProfile(url);
        //System.out.println(profile.getTextures().getSkin().toString());
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwnerProfile(profile); // Set the owning player of the head to the player profile
        head.setItemMeta(meta);
        return head;
    }

    public static void modifyMeta(SkullMeta meta, String url) {
        PlayerProfile profile = getProfile(url);
        meta.setOwnerProfile(profile); // Set the owning player of the head to the player profile
    }

    public static URL getUrlFromBase64(String base64) throws MalformedURLException {
        String decoded = new String(Base64.getDecoder().decode(base64));
        JSONObject obj = new JSONObject(decoded);
        String url = obj.getJSONObject("textures").getJSONObject("SKIN").getString("url");
        return new URL(url);
    }

    public static String getBase64FromUrl(String url) {
        PlayerProfile profile = getProfile(url);
        //Create a JSON object with the skin URL
        JSONObject textures = new JSONObject();
        JSONObject skin = new JSONObject();
        skin.put("url", profile.getTextures().getSkin().toString());
        textures.put("SKIN", skin);
        JSONObject profile2 = new JSONObject();
        profile2.put("textures", textures);
        //Encode the JSON object to Base64
        return Base64.getEncoder().encodeToString(profile2.toString().getBytes());
    }

}
