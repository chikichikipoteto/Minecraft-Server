package com.minecraft.server.world;

/**
 * チャンクを表すクラス（スタブ）
 */
public class Chunk {
    
    private final World world;
    private final int chunkX;
    private final int chunkZ;
    private final int[][][] blocks = new int[16][256][16]; // x, y, z
    
    public Chunk(World world, int chunkX, int chunkZ) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
    
    public int getBlock(int x, int y, int z) {
        if (x < 0 || x >= 16 || y < 0 || y >= 256 || z < 0 || z >= 16) {
            return 0; // 空気ブロック
        }
        return blocks[x][y][z];
    }
    
    public void setBlock(int x, int y, int z, int blockId) {
        if (x < 0 || x >= 16 || y < 0 || y >= 256 || z < 0 || z >= 16) {
            return;
        }
        blocks[x][y][z] = blockId;
    }
    
    public int getChunkX() {
        return chunkX;
    }
    
    public int getChunkZ() {
        return chunkZ;
    }
    
    public World getWorld() {
        return world;
    }
} 