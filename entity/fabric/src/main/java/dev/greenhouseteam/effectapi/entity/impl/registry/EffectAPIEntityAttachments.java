
package dev.greenhouseteam.effectapi.entity.impl.registry;

import dev.greenhouseteam.effectapi.entity.api.attachment.EntityEffectsAttachment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class EffectAPIEntityAttachments {
    public static final AttachmentType<EntityEffectsAttachment> ENTITY_EFFECTS = AttachmentRegistry.<EntityEffectsAttachment>builder()
            .initializer(EntityEffectsAttachment::new)
            .copyOnDeath()
            .buildAndRegister(EntityEffectsAttachment.ID);

    public static void init() {

    }
}
