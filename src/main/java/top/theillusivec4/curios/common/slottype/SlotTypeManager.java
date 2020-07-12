package top.theillusivec4.curios.common.slottype;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeInfo;
import top.theillusivec4.curios.api.SlotTypeInfo.BuildScheme;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.common.slottype.SlotType.Builder;
import top.theillusivec4.curios.server.CuriosConfig;
import top.theillusivec4.curios.server.CuriosConfig.CurioSetting;

public class SlotTypeManager {

  private static Map<String, Builder> queueBuilders = new HashMap<>();
  private static Map<String, Builder> configBuilders = new HashMap<>();

  public static void buildQueuedSlotTypes() {
    queueBuilders.clear();

    for (Pair<BuildScheme, SlotTypeInfo> pair : CuriosApi.getSlotTypeQueue()) {
      SlotTypeInfo info = pair.getRight();
      String id = info.getIdentifier();
      Builder builder = queueBuilders.get(id);

      if (builder == null && pair.getLeft() == BuildScheme.REGISTER) {
        builder = new Builder(id);
        queueBuilders.put(id, builder);
      }

      if (builder != null) {
        builder.size(info.getSize()).locked(info.isLocked()).visible(info.isVisible())
            .hasCosmetic(info.hasCosmetic());
        SlotTypeInfo.Builder preset = SlotTypePreset.findPreset(id)
            .map(SlotTypePreset::getInfoBuilder).orElse(null);
        SlotTypeInfo presetInfo = preset != null ? preset.build() : null;

        if (info.getIcon() == null && presetInfo != null) {
          builder.icon(presetInfo.getIcon());
        } else {
          builder.icon(info.getIcon());
        }

        if (info.getPriority() == null && presetInfo != null) {
          builder.priority(presetInfo.getPriority());
        } else {
          builder.priority(info.getPriority());
        }
      }
    }
  }

  public static void buildConfigSlotTypes() {
    configBuilders.clear();
    Map<String, CurioSetting> settings = CuriosConfig.curios;

    if (settings == null) {
      return;
    }

    settings.forEach((identifier, setting) -> {
      Builder builder = queueBuilders.get(identifier);
      boolean force = setting.override != null ? setting.override : false;

      if (builder == null) {
        builder = new Builder(identifier);
        SlotTypeInfo.Builder preset = SlotTypePreset.findPreset(identifier)
            .map(SlotTypePreset::getInfoBuilder).orElse(null);

        if (preset != null) {
          SlotTypeInfo msg = preset.build();
          builder.icon(msg.getIcon()).priority(msg.getPriority()).size(msg.getSize())
              .locked(msg.isLocked()).visible(msg.isVisible()).hasCosmetic(msg.hasCosmetic());
        }
      } else {
        builder = new Builder(identifier).copyFrom(builder);
      }
      configBuilders.putIfAbsent(identifier, builder);

      if (setting.priority != null) {
        builder.priority(setting.priority, force);
      }

      if (setting.icon != null && !setting.icon.isEmpty()) {
        builder.icon(new Identifier(setting.icon));
      }

      if (setting.size != null) {
        builder.size(setting.size, force);
      }

      if (setting.locked != null) {
        builder.locked(setting.locked, force);
      }

      if (setting.visible != null) {
        builder.visible(setting.visible, force);
      }

      if (setting.hasCosmetic != null) {
        builder.hasCosmetic(setting.hasCosmetic, force);
      }
    });
    queueBuilders.forEach((key, builder) -> configBuilders.putIfAbsent(key, builder));
  }

  public static void buildSlotTypes() {
    Map<String, Builder> builders = !configBuilders.isEmpty() ? configBuilders : queueBuilders;
    builders.values().forEach(builder -> CuriosApi.getSlotHelper().addSlotType(builder.build()));
  }
}