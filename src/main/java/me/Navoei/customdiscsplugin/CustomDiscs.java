package me.Navoei.customdiscsplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.Navoei.customdiscsplugin.command.CustomDiscCommand;
import me.Navoei.customdiscsplugin.event.JukeBox;
import me.Navoei.customdiscsplugin.event.HeadPlay;
import me.Navoei.customdiscsplugin.event.HornPlay;
import me.Navoei.customdiscsplugin.language.Lang;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Jukebox;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CustomDiscs extends JavaPlugin {
    // RU: Статическая переменная для хранения экземпляра плагина
    // EN: Static variable to store the plugin instance
    static CustomDiscs instance;
    
    // RU: Плагин голосового чата (может быть null)
    // EN: Voice chat plugin (can be null)
    @Nullable
    private VoicePlugin voicechatPlugin;
    
    // RU: Логгер плагина
    // EN: Plugin logger
    private Logger pluginLogger;
    
    // RU: Режим отладки
    // EN: Debug mode
    private static boolean debugMode = false;
    
    // RU: Конфигурация файла lang.yml
    // EN: Configuration for lang.yml file
    public static YamlConfiguration LANG;
    
    // RU: Файл lang.yml
    // EN: lang.yml file
    public static File LANG_FILE;
    
    // RU: Включение музыкальных дисков
    // EN: Enable music discs
    public static boolean musicDiscEnable = true;
    
    // RU: Включение проигрывания музыкальных дисков
    // EN: Enable music disc playing
    public static boolean musicDiscPlayingEnable = true;
    
    // RU: Дистанция слышимости музыкальных дисков
    // EN: Music disc hearing distance
    public float musicDiscDistance;
    
    // RU: Максимальная дистанция слышимости музыкальных дисков
    // EN: Maximum music disc hearing distance
    public float musicDiscMaxDistance;
    
    // RU: Громкость музыкальных дисков
    // EN: Music disc volume
    public float musicDiscVolume;
    
    // RU: Включение кастомных горнов
    // EN: Enable custom horns
    public static boolean customHornEnable = true;
    
    // RU: Включение проигрывания кастомных горнов
    // EN: Enable custom horn playing
    public static boolean customHornPlayingEnable = true;
    
    // RU: Дистанция слышимости кастомных горнов
    // EN: Custom horn hearing distance
    public float customHornDistance;
    
    // RU: Максимальная дистанция слышимости кастомных горнов
    // EN: Maximum custom horn hearing distance
    public float customHornMaxDistance;
    
    // RU: Кулдаун горна
    // EN: Horn cooldown
    public int hornCooldown;
    
    // RU: Максимальный кулдаун горна
    // EN: Maximum horn cooldown
    public int hornMaxCooldown;
    
    // RU: Включение кастомных голов
    // EN: Enable custom heads
    public static boolean customHeadEnable = true;
    
    // RU: Включение проигрывания кастомных голов
    // EN: Enable custom head playing
    public static boolean customHeadPlayingEnable = true;
    
    // RU: Дистанция слышимости кастомных голов
    // EN: Custom head hearing distance
    public float customHeadDistance;
    
    // RU: Максимальная дистанция слышимости кастомных голов
    // EN: Maximum custom head hearing distance
    public float customHeadMaxDistance;
    
    @Override
    public void onLoad() {
        // RU: Сохранение экземпляра плагина
        // EN: Store the plugin instance
        CustomDiscs.instance = this;
    }
    
    @Override
    public void onEnable() {
        // RU: Инициализация логгера
        // EN: Initialize the logger
        pluginLogger = getLogger();
        
        // RU: Регистрация команды /customdiscs (перенесено из onLoad для совместимости)
        // EN: Register the /customdiscs command (moved from onLoad for compatibility)
        new CustomDiscCommand(this).register("customdiscs");
        
        // RU: Загрузка сервиса голосового чата
        // EN: Load the voice chat service
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        
        // RU: Сохранение конфигурации по умолчанию
        // EN: Save default configuration
        this.saveDefaultConfig();
        
        // RU: Загрузка файла lang.yml
        // EN: Load the lang.yml file
        loadLang();

        // RU: Инициализация настроек из конфига
        // EN: Initialize settings from config
        debugMode = getConfig().getBoolean("debugMode", false);
        musicDiscEnable = getConfig().getBoolean("music-disc-enable");
        musicDiscPlayingEnable = getConfig().getBoolean("music-disc-playing-enable");
        musicDiscDistance = getConfig().getInt("music-disc-distance");
        musicDiscMaxDistance = getConfig().getInt("music-disc-max-distance");
        musicDiscVolume = Float.parseFloat(Objects.requireNonNull(getConfig().getString("music-disc-volume")));
        customHornEnable = getConfig().getBoolean("custom-horn-enable");
        customHornPlayingEnable = getConfig().getBoolean("custom-horn-playing-enable");
        customHornDistance = getConfig().getInt("custom-horn-distance");
        customHornMaxDistance = getConfig().getInt("custom-horn-max-distance");
        hornCooldown = getConfig().getInt("horn-cooldown");
        hornMaxCooldown = getConfig().getInt("horn-max-cooldown");
        customHeadEnable = getConfig().getBoolean("custom-head-enable");
        customHeadPlayingEnable = getConfig().getBoolean("custom-head-playing-enable");
        customHeadDistance = getConfig().getInt("custom-head-distance");
        customHeadMaxDistance = getConfig().getInt("custom-head-max-distance");

        // RU: Создание папки musicdata, если она не существует
        // EN: Create the musicdata folder if it doesn't exist
        File musicData = new File(this.getDataFolder(), "musicdata");
        if (!(musicData.exists())) {
            musicData.mkdirs();
        }
        
        // RU: Регистрация плагина для голосового чата
        // EN: Register the plugin for voice chat
        if (service != null) {
            voicechatPlugin = new VoicePlugin();
            service.registerPlugin(voicechatPlugin);
            pluginLogger.info("Плагин CustomDiscs успешно зарегистрирован / CustomDiscs plugin successfully registered");
        } else {
            pluginLogger.info("Не удалось зарегистрировать плагин CustomDiscs / Failed to register CustomDiscs plugin");
        }

        // RU: Регистрация событий для музыкальных дисков
        // EN: Register events for music discs
        if (isMusicDiscEnable()) {
            getServer().getPluginManager().registerEvents(new JukeBox(), this);
            getServer().getPluginManager().registerEvents(new HopperManager(), this);
        }
        
        // RU: Регистрация событий для кастомных голов
        // EN: Register events for custom heads
        if (isCustomHeadEnable()) {
            getServer().getPluginManager().registerEvents(new HeadPlay(), this);
        }
        
        // RU: Регистрация событий для кастомных горнов
        // EN: Register events for custom horns
        if (isCustomHornEnable()) {
            getServer().getPluginManager().registerEvents(new HornPlay(), this);
        }

        // RU: Установка минимального кулдауна для горнов
        // EN: Set minimum cooldown for horns
        if (hornCooldown <= 0) {
            hornCooldown = 1;
        }
        if (hornMaxCooldown <= 0) {
            hornMaxCooldown = 1;
        }

        // RU: Настройка ProtocolLib для обработки пакетов
        // EN: Set up ProtocolLib for packet handling
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.WORLD_EVENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                // RU: Проверка пакета для события проигрывания диска
                // EN: Check packet for disc playback event
                if (packet.getIntegers().read(0).toString().equals("1010")) {
                    if (!isMusicDiscEnable()) { return; }
                    Jukebox jukebox = (Jukebox) packet.getBlockPositionModifier().read(0).toLocation(event.getPlayer().getWorld()).getBlock().getState();

                    if (!jukebox.getRecord().hasItemMeta()) return;

                    // RU: Проверка, является ли диск кастомным
                    // EN: Check if the disc is custom
                    if (jukebox.getRecord().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "customdisc"), PersistentDataType.STRING)) {
                        event.setCancelled(true);
                    }

                    // RU: Запуск менеджера состояния проигрывателя
                    // EN: Start the jukebox state manager
                    JukeboxStateManager.start(jukebox);
                }
            }
        });
    }
    
    @Override
    public void onDisable() {
        // RU: Отмена регистрации плагина голосового чата
        // EN: Unregister the voice chat plugin
        if (voicechatPlugin != null) {
            getServer().getServicesManager().unregister(voicechatPlugin);
            pluginLogger.info("Плагин CustomDiscs успешно отключен / CustomDiscs plugin successfully unregistered");
        }
    }
    
    // RU: Получение экземпляра плагина
    // EN: Get the plugin instance
    public static CustomDiscs getInstance() {
        return instance;
    }
        
    /**
     * RU: Загрузка файла lang.yml
     * EN: Load the lang.yml file
     */
    public void loadLang() {
        File lang = new File(getDataFolder(), "lang.yml");
        if (!lang.exists()) {
            try {
                // RU: Создание папки и файла lang.yml
                // EN: Create folder and lang.yml file
                getDataFolder().mkdir();
                lang.createNewFile();
                InputStream defConfigStream = this.getResource("lang.yml");
                if (defConfigStream != null) {
                    copyInputStreamToFile(defConfigStream, lang);
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(lang);
                    defConfig.save(lang);
                    Lang.setFile(defConfig);
                }
            } catch (IOException e) {
                pluginLogger.severe("Не удалось создать lang.yml для CustomDiscs / Failed to create lang.yml for CustomDiscs");
                pluginLogger.severe("Отключение плагина... / Disabling plugin...");
                this.setEnabled(false); // RU: Без lang.yml плагин отключается / EN: Disable plugin without lang.yml
                if (isDebugMode()) {
                    pluginLogger.log(Level.SEVERE, "Вывод ошибки / Exception output: ", e);
                }
            }
        }
        // RU: Загрузка и настройка конфигурации lang.yml
        // EN: Load and configure lang.yml
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
        for (Lang item : Lang.values()) {
            if (conf.getString(item.getPath()) == null) {
                conf.set(item.getPath(), item.getDefault());
            }
        }
        Lang.setFile(conf);
        LANG = conf;
        LANG_FILE = lang;
        try {
            conf.save(getLangFile());
        } catch (IOException e) {
            pluginLogger.warning("Не удалось сохранить lang.yml для CustomDiscs / Failed to save lang.yml for CustomDiscs");
            pluginLogger.warning("Отключение плагина... / Disabling plugin...");
            if (isDebugMode()) {
                pluginLogger.log(Level.SEVERE, "Вывод ошибки / Exception output: ", e);
            }
        }
    }
    
    /**
     * RU: Получение конфигурации lang.yml
     * EN: Get the lang.yml configuration
     *
     * @return Конфигурация lang.yml / The lang.yml configuration
     */
    public YamlConfiguration getLang() {
        return LANG;
    }
    
    /**
     * RU: Получение файла lang.yml
     * EN: Get the lang.yml file
     *
     * @return Файл lang.yml / The lang.yml file
     */
    public File getLangFile() {
        return LANG_FILE;
    }
    
    /**
     * RU: Копирование InputStream в файл
     * EN: Copy InputStream to file
     */
    public static void copyInputStreamToFile(InputStream input, File file) {
        try (OutputStream output = new FileOutputStream(file)) {
            input.transferTo(output);
        } catch (IOException ioException) {
            if (isDebugMode()) {
                CustomDiscs.getInstance().getLogger().log(Level.SEVERE, "Вывод ошибки / Exception output: ", ioException);
            }
        }
    }

    /**
     * RU: Получение значения debugMode из конфигурации
     * EN: Get the debugMode configuration value
     *
     * @return Значение debugMode / The debugMode value
     */
    public static boolean isDebugMode() {
        return debugMode;
    }

    /**
     * RU: Получение значения musicDiscEnable из конфигурации
     * EN: Get the musicDiscEnable configuration value
     *
     * @return Значение musicDiscEnable / The musicDiscEnable value
     */
    public static boolean isMusicDiscEnable() {
        return musicDiscEnable;
    }

    /**
     * RU: Получение значения customHornEnable из конфигурации
     * EN: Get the customHornEnable configuration value
     *
     * @return Значение customHornEnable / The customHornEnable value
     */
    public static boolean isCustomHornEnable() {
        return customHornEnable;
    }

    /**
     * RU: Получение значения customHeadEnable из конфигурации
     * EN: Get the customHeadEnable configuration value
     *
     * @return Значение customHeadEnable / The customHeadEnable value
     */
    public static boolean isCustomHeadEnable() {
        return customHeadEnable;
    }

    /**
     * RU: Получение значения musicDiscPlayingEnable из конфигурации
     * EN: Get the musicDiscPlayingEnable configuration value
     *
     * @return Значение musicDiscPlayingEnable / The musicDiscPlayingEnable value
     */
    public static boolean isMusicDiscPlayingEnable() {
        return musicDiscPlayingEnable;
    }

    /**
     * RU: Получение значения customHornPlayingEnable из конфигурации
     * EN: Get the customHornPlayingEnable configuration value
     *
     * @return Значение customHornPlayingEnable / The customHornPlayingEnable value
     */
    public static boolean isCustomHornPlayingEnable() {
        return customHornPlayingEnable;
    }

    /**
     * RU: Получение значения customHeadPlayingEnable из конфигурации
     * EN: Get the customHeadPlayingEnable configuration value
     *
     * @return Значение customHeadPlayingEnable / The customHeadPlayingEnable value
     */
    public static boolean isCustomHeadPlayingEnable() {
        return customHeadPlayingEnable;
    }
}
