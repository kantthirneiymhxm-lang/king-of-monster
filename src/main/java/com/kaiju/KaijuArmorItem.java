package com.kaiju;

import net.minecraft.item.ArmorItem;

public class KaijuArmorItem extends ArmorItem {
    public final String kind;
    public KaijuArmorItem(KaijuArmorMaterial m, Type type, Settings s) { super(m, type, s); this.kind = m.kind; }
}
