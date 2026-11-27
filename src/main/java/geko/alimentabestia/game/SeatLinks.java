package geko.alimentabestia.game;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeatLinks extends SavedData {
    private static final String FILE_NAME = "geko_alimentabestia_asientos";
    private static final String TAG_LINKS = "Enlaces";
    private static final String TAG_SEAT = "Silla";
    private static final String TAG_FOCA = "Foca";
    private static final String TAG_NEXT_ID = "SiguienteIdFoca";

    private final Map<BlockPos, UUID> links = new HashMap<>();
    private int nextFocaId = 1;

    public SeatLinks() {
    }

    public static SeatLinks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(SeatLinks::load, SeatLinks::new, FILE_NAME);
    }

    private static SeatLinks load(CompoundTag tag) {
        SeatLinks data = new SeatLinks();
        ListTag list = tag.getList(TAG_LINKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID(TAG_FOCA)) {
                data.links.put(BlockPos.of(entry.getLong(TAG_SEAT)), entry.getUUID(TAG_FOCA));
            }
        }
        data.nextFocaId = Math.max(1, tag.getInt(TAG_NEXT_ID));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, UUID> entry : links.entrySet()) {
            CompoundTag item = new CompoundTag();
            item.putLong(TAG_SEAT, entry.getKey().asLong());
            item.putUUID(TAG_FOCA, entry.getValue());
            list.add(item);
        }
        tag.put(TAG_LINKS, list);
        tag.putInt(TAG_NEXT_ID, nextFocaId);
        return tag;
    }

    public int takeFocaId() {
        int id = nextFocaId++;
        setDirty();
        return id;
    }

    public void link(BlockPos seat, UUID foca) {
        links.put(seat.immutable(), foca);
        setDirty();
    }

    public boolean unlink(BlockPos seat) {
        boolean removed = links.remove(seat) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public UUID focaAt(BlockPos seat) {
        return links.get(seat);
    }

    public BlockPos seatOf(UUID foca) {
        for (Map.Entry<BlockPos, UUID> entry : links.entrySet()) {
            if (entry.getValue().equals(foca)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
