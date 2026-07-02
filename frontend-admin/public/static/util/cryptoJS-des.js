(function() {
    // Shortcuts
    var C = CryptoJS;
    var C_lib = C.lib;
    var WordArray = C_lib.WordArray;
    var BlockCipher = C_lib.BlockCipher;
    var C_algo = C.algo;

    // Permuted Choice 1 constants
    var PC1 = [
        57, 49, 41, 33, 25, 17, 9, 1,
        58, 50, 42, 34, 26, 18, 10, 2,
        59, 51, 43, 35, 27, 19, 11, 3,
        60, 52, 44, 36, 63, 55, 47, 39,
        31, 23, 15, 7, 62, 54, 46, 38,
        30, 22, 14, 6, 61, 53, 45, 37,
        29, 21, 13, 5, 28, 20, 12, 4
    ];

    // Permuted Choice 2 constants
    var PC2 = [
        14, 17, 11, 24, 1, 5,
        3, 28, 15, 6, 21, 10,
        23, 19, 12, 4, 26, 8,
        16, 7, 27, 20, 13, 2,
        41, 52, 31, 37, 47, 55,
        30, 40, 51, 45, 33, 48,
        44, 49, 39, 56, 34, 53,
        46, 42, 50, 36, 29, 32
    ];

    // Cumulative bit shift constants
    var BIT_SHIFTS = [1, 2, 4, 6, 8, 10, 12, 14, 15, 17, 19, 21, 23, 25, 27, 28];

    // SBOXes and round permutation constants
    var SBOX_P = [{
        0x0: 0x808200,
        0x10000000: 0x8000,
        0x20000000: 0x808002,
        0x30000000: 0x2,
        0x40000000: 0x200,
        0x50000000: 0x808202,
        0x60000000: 0x800202,
        0x70000000: 0x800000,
        0x80000000: 0x202,
        0x90000000: 0x800200,
        0xa0000000: 0x8200,
        0xb0000000: 0x808000,
        0xc0000000: 0x8002,
        0xd0000000: 0x800002,
        0xe0000000: 0x0,
        0xf0000000: 0x8202,
        0x8000000: 0x0,
        0x18000000: 0x808202,
        0x28000000: 0x8202,
        0x38000000: 0x8000,
        0x48000000: 0x808200,
        0x58000000: 0x200,
        0x68000000: 0x808002,
        0x78000000: 0x2,
        0x88000000: 0x800200,
        0x98000000: 0x8200,
        0xa8000000: 0x808000,
        0xb8000000: 0x800202,
        0xc8000000: 0x800002,
        0xd8000000: 0x8002,
        0xe8000000: 0x202,
        0xf8000000: 0x800000,
        0x1: 0x8000,
        0x10000001: 0x2,
        0x20000001: 0x808200,
        0x30000001: 0x800000,
        0x40000001: 0x808002,
        0x50000001: 0x8200,
        0x60000001: 0x200,
        0x70000001: 0x800202,
        0x80000001: 0x808202,
        0x90000001: 0x808000,
        0xa0000001: 0x800002,
        0xb0000001: 0x8202,
        0xc0000001: 0x202,
        0xd0000001: 0x800200,
        0xe0000001: 0x8002,
        0xf0000001: 0x0,
        0x8000001: 0x808202,
        0x18000001: 0x808000,
        0x28000001: 0x800000,
        0x38000001: 0x200,
        0x48000001: 0x8000,
        0x58000001: 0x800002,
        0x68000001: 0x2,
        0x78000001: 0x8202,
        0x88000001: 0x8002,
        0x98000001: 0x800202,
        0xa8000001: 0x202,
        0xb8000001: 0x808200,
        0xc8000001: 0x800200,
        0xd8000001: 0x0,
        0xe8000001: 0x8200,
        0xf8000001: 0x808002
    },
        {
            0x0: 0x40084010,
            0x1000000: 0x4000,
            0x2000000: 0x80000,
            0x3000000: 0x40080010,
            0x4000000: 0x40000010,
            0x5000000: 0x40084000,
            0x6000000: 0x40004000,
            0x7000000: 0x10,
            0x8000000: 0x84000,
            0x9000000: 0x40004010,
            0xa000000: 0x40000000,
            0xb000000: 0x84010,
            0xc000000: 0x80010,
            0xd000000: 0x0,
            0xe000000: 0x4010,
            0xf000000: 0x40080000,
            0x800000: 0x40004000,
            0x1800000: 0x84010,
            0x2800000: 0x10,
            0x3800000: 0x40004010,
            0x4800000: 0x40084010,
            0x5800000: 0x40000000,
            0x6800000: 0x80000,
            0x7800000: 0x40080010,
            0x8800000: 0x80010,
            0x9800000: 0x0,
            0xa800000: 0x4000,
            0xb800000: 0x40080000,
            0xc800000: 0x40000010,
            0xd800000: 0x84000,
            0xe800000: 0x40084000,
            0xf800000: 0x4010,
            0x10000000: 0x0,
            0x11000000: 0x40080010,
            0x12000000: 0x40004010,
            0x13000000: 0x40084000,
            0x14000000: 0x40080000,
            0x15000000: 0x10,
            0x16000000: 0x84010,
            0x17000000: 0x4000,
            0x18000000: 0x4010,
            0x19000000: 0x80000,
            0x1a000000: 0x80010,
            0x1b000000: 0x40000010,
            0x1c000000: 0x84000,
            0x1d000000: 0x40004000,
            0x1e000000: 0x40000000,
            0x1f000000: 0x40084010,
            0x10800000: 0x84010,
            0x11800000: 0x80000,
            0x12800000: 0x40080000,
            0x13800000: 0x4000,
            0x14800000: 0x40004000,
            0x15800000: 0x40084010,
            0x16800000: 0x10,
            0x17800000: 0x40000000,
            0x18800000: 0x40084000,
            0x19800000: 0x40000010,
            0x1a800000: 0x40004010,
            0x1b800000: 0x80010,
            0x1c800000: 0x0,
            0x1d800000: 0x4010,
            0x1e800000: 0x40080010,
            0x1f800000: 0x84000
        },
        {
            0x0: 0x104,
            0x100000: 0x0,
            0x200000: 0x4000100,
            0x300000: 0x10104,
            0x400000: 0x10004,
            0x500000: 0x4000004,
            0x600000: 0x4010104,
            0x700000: 0x4010000,
            0x800000: 0x4000000,
            0x900000: 0x4010100,
            0xa00000: 0x10100,
            0xb00000: 0x4010004,
            0xc00000: 0x4000104,
            0xd00000: 0x10000,
            0xe00000: 0x4,
            0xf00000: 0x100,
            0x80000: 0x4010100,
            0x180000: 0x4010004,
            0x280000: 0x0,
            0x380000: 0x4000100,
            0x480000: 0x4000004,
            0x580000: 0x10000,
            0x680000: 0x10004,
            0x780000: 0x104,
            0x880000: 0x4,
            0x980000: 0x100,
            0xa80000: 0x4010000,
            0xb80000: 0x10104,
            0xc80000: 0x10100,
            0xd80000: 0x4000104,
            0xe80000: 0x4010104,
            0xf80000: 0x4000000,
            0x1000000: 0x4010100,
            0x1100000: 0x10004,
            0x1200000: 0x10000,
            0x1300000: 0x4000100,
            0x1400000: 0x100,
            0x1500000: 0x4010104,
            0x1600000: 0x4000004,
            0x1700000: 0x0,
            0x1800000: 0x4000104,
            0x1900000: 0x4000000,
            0x1a00000: 0x4,
            0x1b00000: 0x10100,
            0x1c00000: 0x4010000,
            0x1d00000: 0x104,
            0x1e00000: 0x10104,
            0x1f00000: 0x4010004,
            0x1080000: 0x4000000,
            0x1180000: 0x104,
            0x1280000: 0x4010100,
            0x1380000: 0x0,
            0x1480000: 0x10004,
            0x1580000: 0x4000100,
            0x1680000: 0x100,
            0x1780000: 0x4010004,
            0x1880000: 0x10000,
            0x1980000: 0x4010104,
            0x1a80000: 0x10104,
            0x1b80000: 0x4000004,
            0x1c80000: 0x4000104,
            0x1d80000: 0x4010000,
            0x1e80000: 0x4,
            0x1f80000: 0x10100
        },
        {
            0x0: 0x80401000,
            0x10000: 0x80001040,
            0x20000: 0x401040,
            0x30000: 0x80400000,
            0x40000: 0x0,
            0x50000: 0x401000,
            0x60000: 0x80000040,
            0x70000: 0x400040,
            0x80000: 0x80000000,
            0x90000: 0x400000,
            0xa0000: 0x40,
            0xb0000: 0x80001000,
            0xc0000: 0x80400040,
            0xd0000: 0x1040,
            0xe0000: 0x1000,
            0xf0000: 0x80401040,
            0x8000: 0x80001040,
            0x18000: 0x40,
            0x28000: 0x80400040,
            0x38000: 0x80001000,
            0x48000: 0x401000,
            0x58000: 0x80401040,
            0x68000: 0x0,
            0x78000: 0x80400000,
            0x88000: 0x1000,
            0x98000: 0x80401000,
            0xa8000: 0x400000,
            0xb8000: 0x1040,
            0xc8000: 0x80000000,
            0xd8000: 0x400040,
            0xe8000: 0x401040,
            0xf8000: 0x80000040,
            0x100000: 0x400040,
            0x110000: 0x401000,
            0x120000: 0x80000040,
            0x130000: 0x0,
            0x140000: 0x1040,
            0x150000: 0x80400040,
            0x160000: 0x80401000,
            0x170000: 0x80001040,
            0x180000: 0x80401040,
            0x190000: 0x80000000,
            0x1a0000: 0x80400000,
            0x1b0000: 0x401040,
            0x1c0000: 0x80001000,
            0x1d0000: 0x400000,
            0x1e0000: 0x40,
            0x1f0000: 0x1000,
            0x108000: 0x80400000,
            0x118000: 0x80401040,
            0x128000: 0x0,
            0x138000: 0x401000,
            0x148000: 0x400040,
            0x158000: 0x80000000,
            0x168000: 0x80001040,
            0x178000: 0x40,
            0x188000: 0x80000040,
            0x198000: 0x1000,
            0x1a8000: 0x80001000,
            0x1b8000: 0x80400040,
            0x1c8000: 0x1040,
            0x1d8000: 0x80401000,
            0x1e8000: 0x400000,
            0x1f8000: 0x401040
        },
        {
            0x0: 0x80,
            0x1000: 0x1040000,
            0x2000: 0x40000,
            0x3000: 0x20000000,
            0x4000: 0x20040080,
            0x5000: 0x1000080,
            0x6000: 0x21000080,
            0x7000: 0x40080,
            0x8000: 0x1000000,
            0x9000: 0x20040000,
            0xa000: 0x20000080,
            0xb000: 0x21040080,
            0xc000: 0x21040000,
            0xd000: 0x0,
            0xe000: 0x1040080,
            0xf000: 0x21000000,
            0x800: 0x1040080,
            0x1800: 0x21000080,
            0x2800: 0x80,
            0x3800: 0x1040000,
            0x4800: 0x40000,
            0x5800: 0x20040080,
            0x6800: 0x21040000,
            0x7800: 0x20000000,
            0x8800: 0x20040000,
            0x9800: 0x0,
            0xa800: 0x21040080,
            0xb800: 0x1000080,
            0xc800: 0x20000080,
            0xd800: 0x21000000,
            0xe800: 0x1000000,
            0xf800: 0x40080,
            0x10000: 0x40000,
            0x11000: 0x80,
            0x12000: 0x20000000,
            0x13000: 0x21000080,
            0x14000: 0x1000080,
            0x15000: 0x21040000,
            0x16000: 0x20040080,
            0x17000: 0x1000000,
            0x18000: 0x21040080,
            0x19000: 0x21000000,
            0x1a000: 0x1040000,
            0x1b000: 0x20040000,
            0x1c000: 0x40080,
            0x1d000: 0x20000080,
            0x1e000: 0x0,
            0x1f000: 0x1040080,
            0x10800: 0x21000080,
            0x11800: 0x1000000,
            0x12800: 0x1040000,
            0x13800: 0x20040080,
            0x14800: 0x20000000,
            0x15800: 0x1040080,
            0x16800: 0x80,
            0x17800: 0x21040000,
            0x18800: 0x40080,
            0x19800: 0x21040080,
            0x1a800: 0x0,
            0x1b800: 0x21000000,
            0x1c800: 0x1000080,
            0x1d800: 0x40000,
            0x1e800: 0x20040000,
            0x1f800: 0x20000080
        },
        {
            0x0: 0x10000008,
            0x100: 0x2000,
            0x200: 0x10200000,
            0x300: 0x10202008,
            0x400: 0x10002000,
            0x500: 0x200000,
            0x600: 0x200008,
            0x700: 0x10000000,
            0x800: 0x0,
            0x900: 0x10002008,
            0xa00: 0x202000,
            0xb00: 0x8,
            0xc00: 0x10200008,
            0xd00: 0x202008,
            0xe00: 0x2008,
            0xf00: 0x10202000,
            0x80: 0x10200000,
            0x180: 0x10202008,
            0x280: 0x8,
            0x380: 0x200000,
            0x480: 0x202008,
            0x580: 0x10000008,
            0x680: 0x10002000,
            0x780: 0x2008,
            0x880: 0x200008,
            0x980: 0x2000,
            0xa80: 0x10002008,
            0xb80: 0x10200008,
            0xc80: 0x0,
            0xd80: 0x10202000,
            0xe80: 0x202000,
            0xf80: 0x10000000,
            0x1000: 0x10002000,
            0x1100: 0x10200008,
            0x1200: 0x10202008,
            0x1300: 0x2008,
            0x1400: 0x200000,
            0x1500: 0x10000000,
            0x1600: 0x10000008,
            0x1700: 0x202000,
            0x1800: 0x202008,
            0x1900: 0x0,
            0x1a00: 0x8,
            0x1b00: 0x10200000,
            0x1c00: 0x2000,
            0x1d00: 0x10002008,
            0x1e00: 0x10202000,
            0x1f00: 0x200008,
            0x1080: 0x8,
            0x1180: 0x202000,
            0x1280: 0x200000,
            0x1380: 0x10000008,
            0x1480: 0x10002000,
            0x1580: 0x2008,
            0x1680: 0x10202008,
            0x1780: 0x10200000,
            0x1880: 0x10202000,
            0x1980: 0x10200008,
            0x1a80: 0x2000,
            0x1b80: 0x202008,
            0x1c80: 0x200008,
            0x1d80: 0x0,
            0x1e80: 0x10000000,
            0x1f80: 0x10002008
        },
        {
            0x0: 0x100000,
            0x10: 0x2000401,
            0x20: 0x400,
            0x30: 0x100401,
            0x40: 0x2100401,
            0x50: 0x0,
            0x60: 0x1,
            0x70: 0x2100001,
            0x80: 0x2000400,
            0x90: 0x100001,
            0xa0: 0x2000001,
            0xb0: 0x2100400,
            0xc0: 0x2100000,
            0xd0: 0x401,
            0xe0: 0x100400,
            0xf0: 0x2000000,
            0x8: 0x2100001,
            0x18: 0x0,
            0x28: 0x2000401,
            0x38: 0x2100400,
            0x48: 0x100000,
            0x58: 0x2000001,
            0x68: 0x2000000,
            0x78: 0x401,
            0x88: 0x100401,
            0x98: 0x2000400,
            0xa8: 0x2100000,
            0xb8: 0x100001,
            0xc8: 0x400,
            0xd8: 0x2100401,
            0xe8: 0x1,
            0xf8: 0x100400,
            0x100: 0x2000000,
            0x110: 0x100000,
            0x120: 0x2000401,
            0x130: 0x2100001,
            0x140: 0x100001,
            0x150: 0x2000400,
            0x160: 0x2100400,
            0x170: 0x100401,
            0x180: 0x401,
            0x190: 0x2100401,
            0x1a0: 0x100400,
            0x1b0: 0x1,
            0x1c0: 0x0,
            0x1d0: 0x2100000,
            0x1e0: 0x2000001,
            0x1f0: 0x400,
            0x108: 0x100400,
            0x118: 0x2000401,
            0x128: 0x2100001,
            0x138: 0x1,
            0x148: 0x2000000,
            0x158: 0x100000,
            0x168: 0x401,
            0x178: 0x2100400,
            0x188: 0x2000001,
            0x198: 0x2100000,
            0x1a8: 0x0,
            0x1b8: 0x2100401,
            0x1c8: 0x100401,
            0x1d8: 0x400,
            0x1e8: 0x2000400,
            0x1f8: 0x100001
        },
        {
            0x0: 0x8000820,
            0x1: 0x20000,
            0x2: 0x8000000,
            0x3: 0x20,
            0x4: 0x20020,
            0x5: 0x8020820,
            0x6: 0x8020800,
            0x7: 0x800,
            0x8: 0x8020000,
            0x9: 0x8000800,
            0xa: 0x20800,
            0xb: 0x8020020,
            0xc: 0x820,
            0xd: 0x0,
            0xe: 0x8000020,
            0xf: 0x20820,
            0x80000000: 0x800,
            0x80000001: 0x8020820,
            0x80000002: 0x8000820,
            0x80000003: 0x8000000,
            0x80000004: 0x8020000,
            0x80000005: 0x20800,
            0x80000006: 0x20820,
            0x80000007: 0x20,
            0x80000008: 0x8000020,
            0x80000009: 0x820,
            0x8000000a: 0x20020,
            0x8000000b: 0x8020800,
            0x8000000c: 0x0,
            0x8000000d: 0x8020020,
            0x8000000e: 0x8000800,
            0x8000000f: 0x20000,
            0x10: 0x20820,
            0x11: 0x8020800,
            0x12: 0x20,
            0x13: 0x800,
            0x14: 0x8000800,
            0x15: 0x8000020,
            0x16: 0x8020020,
            0x17: 0x20000,
            0x18: 0x0,
            0x19: 0x20020,
            0x1a: 0x8020000,
            0x1b: 0x8000820,
            0x1c: 0x8020820,
            0x1d: 0x20800,
            0x1e: 0x820,
            0x1f: 0x8000000,
            0x80000010: 0x20000,
            0x80000011: 0x800,
            0x80000012: 0x8020020,
            0x80000013: 0x20820,
            0x80000014: 0x20,
            0x80000015: 0x8020000,
            0x80000016: 0x8000000,
            0x80000017: 0x8000820,
            0x80000018: 0x8020820,
            0x80000019: 0x8000020,
            0x8000001a: 0x8000800,
            0x8000001b: 0x0,
            0x8000001c: 0x20800,
            0x8000001d: 0x820,
            0x8000001e: 0x20020,
            0x8000001f: 0x8020800
        }
    ];

    // Masks that select the SBOX input
    var SBOX_MASK = [
        0xf8000001, 0x1f800000, 0x01f80000, 0x001f8000,
        0x0001f800, 0x00001f80, 0x000001f8, 0x8000001f
    ];

    /**
     * DES block cipher algorithm.
     */
    var DES = C_algo.DES = BlockCipher.extend({
        _doReset: function() {
            // Shortcuts
            var key = this._key;
            var keyWords = key.words;

            // Select 56 bits according to PC1
            var keyBits = [];
            for(var i = 0; i < 56; i++) {
                var keyBitPos = PC1[i] - 1;
                keyBits[i] = (keyWords[keyBitPos >>> 5] >>> (31 - keyBitPos % 32)) & 1;
            }

            // Assemble 16 subkeys
            var subKeys = this._subKeys = [];
            for(var nSubKey = 0; nSubKey < 16; nSubKey++) {
                // Create subkey
                var subKey = subKeys[nSubKey] = [];

                // Shortcut
                var bitShift = BIT_SHIFTS[nSubKey];

                // Select 48 bits according to PC2
                for(var i = 0; i < 24; i++) {
                    // Select from the left 28 key bits
                    subKey[(i / 6) | 0] |= keyBits[((PC2[i] - 1) + bitShift) % 28] << (31 - i % 6);

                    // Select from the right 28 key bits
                    subKey[4 + ((i / 6) | 0)] |= keyBits[28 + (((PC2[i + 24] - 1) + bitShift) % 28)] << (31 - i % 6);
                }

                // Since each subkey is applied to an expanded 32-bit input,
                // the subkey can be broken into 8 values scaled to 32-bits,
                // which allows the key to be used without expansion
                subKey[0] = (subKey[0] << 1) | (subKey[0] >>> 31);
                for(var i = 1; i < 7; i++) {
                    subKey[i] = subKey[i] >>> ((i - 1) * 4 + 3);
                }
                subKey[7] = (subKey[7] << 5) | (subKey[7] >>> 27);
            }

            // Compute inverse subkeys
            var invSubKeys = this._invSubKeys = [];
            for(var i = 0; i < 16; i++) {
                invSubKeys[i] = subKeys[15 - i];
            }
        },

        encryptBlock: function(M, offset) {
            this._doCryptBlock(M, offset, this._subKeys);
        },

        decryptBlock: function(M, offset) {
            this._doCryptBlock(M, offset, this._invSubKeys);
        },

        _doCryptBlock: function(M, offset, subKeys) {
            // Get input
            this._lBlock = M[offset];
            this._rBlock = M[offset + 1];

            // Initial permutation
            exchangeLR.call(this, 4, 0x0f0f0f0f);
            exchangeLR.call(this, 16, 0x0000ffff);
            exchangeRL.call(this, 2, 0x33333333);
            exchangeRL.call(this, 8, 0x00ff00ff);
            exchangeLR.call(this, 1, 0x55555555);

            // Rounds
            for(var round = 0; round < 16; round++) {
                // Shortcuts
                var subKey = subKeys[round];
                var lBlock = this._lBlock;
                var rBlock = this._rBlock;

                // Feistel function
                var f = 0;
                for(var i = 0; i < 8; i++) {
                    f |= SBOX_P[i][((rBlock ^ subKey[i]) & SBOX_MASK[i]) >>> 0];
                }
                this._lBlock = rBlock;
                this._rBlock = lBlock ^ f;
            }

            // Undo swap from last round
            var t = this._lBlock;
            this._lBlock = this._rBlock;
            this._rBlock = t;

            // Final permutation
            exchangeLR.call(this, 1, 0x55555555);
            exchangeRL.call(this, 8, 0x00ff00ff);
            exchangeRL.call(this, 2, 0x33333333);
            exchangeLR.call(this, 16, 0x0000ffff);
            exchangeLR.call(this, 4, 0x0f0f0f0f);

            // Set output
            M[offset] = this._lBlock;
            M[offset + 1] = this._rBlock;
        },

        keySize: 64 / 32,

        ivSize: 64 / 32,

        blockSize: 64 / 32
    });

    // Swap bits across the left and right words
    function exchangeLR(offset, mask) {
        var t = ((this._lBlock >>> offset) ^ this._rBlock) & mask;
        this._rBlock ^= t;
        this._lBlock ^= t << offset;
    }

    function exchangeRL(offset, mask) {
        var t = ((this._rBlock >>> offset) ^ this._lBlock) & mask;
        this._lBlock ^= t;
        this._rBlock ^= t << offset;
    }

    /**
     * Shortcut functions to the cipher's object interface.
     *
     * @example
     *
     *     var ciphertext = CryptoJS.DES.encrypt(message, key, cfg);
     *     var plaintext  = CryptoJS.DES.decrypt(ciphertext, key, cfg);
     */
    C.DES = BlockCipher._createHelper(DES);

    /**
     * Triple-DES block cipher algorithm.
     */
    var TripleDES = C_algo.TripleDES = BlockCipher.extend({
        _doReset: function() {
            // Shortcuts
            var key = this._key;
            var keyWords = key.words;

            // Create DES instances
            this._des1 = DES.createEncryptor(WordArray.create(keyWords.slice(0, 2)));
            this._des2 = DES.createEncryptor(WordArray.create(keyWords.slice(2, 4)));
            this._des3 = DES.createEncryptor(WordArray.create(keyWords.slice(4, 6)));
        },

        encryptBlock: function(M, offset) {
            this._des1.encryptBlock(M, offset);
            this._des2.decryptBlock(M, offset);
            this._des3.encryptBlock(M, offset);
        },

        decryptBlock: function(M, offset) {
            this._des3.decryptBlock(M, offset);
            this._des2.encryptBlock(M, offset);
            this._des1.decryptBlock(M, offset);
        },

        keySize: 192 / 32,

        ivSize: 64 / 32,

        blockSize: 64 / 32
    });

    /**
     * Shortcut functions to the cipher's object interface.
     *
     * @example
     *
     *     var ciphertext = CryptoJS.TripleDES.encrypt(message, key, cfg);
     *     var plaintext  = CryptoJS.TripleDES.decrypt(ciphertext, key, cfg);
     */
    C.TripleDES = BlockCipher._createHelper(TripleDES);
}());

//js 加密方法
;var encode_version = 'jsjiami.com.v5', ivjnd = '__0x95308',  __0x95308=['w6tCNg==','w5zDjsOyD8Oh','FArDg8KFwqI=','bB7CoMKkw6g=','XcK5Wn/Dog==','w7d4Z8Ocw5o=','woDCnsK5UcKe','w6fClgXDpg==','M8K1H8Oew6zDocKI','w7PDgDkl','w6/Cg1E=','C8KCw7Q=','dRdTwqUx','54ih5pyW5YyT772UwpPDgOS+ueWukuafkOW9qOeruO++p+i9jeivoeaWnuaOsuaIu+S7hOeZs+W2m+S9ng==','5Yqs6ZuB54uZ5p2L5Y2a77y3WsK+5L2l5a+85pyg5b6A56qj','A0xyME3CkHIPwrw=','wrt/QA==','LE7DvSo=','w6crHGPCuQ==','w5Rffw==','w4fCr3fDsA==','NMKYw5M=','fsKKSUXCoA==','wqp+cMOqC2bDv8KC','DcOjKQ==','w7R/T8Oiw4TCgDA=','eiVH','wp7DsMOEJlU=','E8KYPg==','IcKCwps=','SMKrw489wp0=','MQNfwpjDlHHDgAA=','wo5FFA==','w4vDmzt4','FcKPCg==','54qj5pyH5Y6A772gBsOC5L6x5a+T5p2j5b+D56qb77ye6LyV6K6U5paJ5oyX5oqF5Luo55ub5bas5L62','w5NUHcO8MUx8DMKd','w5RURB/DoGXCosK6w5PCosOMXMOhw5I='];(function(_0x5a5138,_0x3fb5df){var _0x5c9f59=function(_0x1452db){while(--_0x1452db){_0x5a5138['push'](_0x5a5138['shift']());}};_0x5c9f59(++_0x3fb5df);}(__0x95308,0x119));var _0x4dca=function(_0x581088,_0x3c5cdd){_0x581088=_0x581088-0x0;var _0x5187db=__0x95308[_0x581088];if(_0x4dca['initialized']===undefined){(function(){var _0x2a3311=typeof window!=='undefined'?window:typeof process==='object'&&typeof require==='function'&&typeof global==='object'?global:this;var _0xf28d15='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';_0x2a3311['atob']||(_0x2a3311['atob']=function(_0x2fdc7c){var _0x1783b7=String(_0x2fdc7c)['replace'](/=+$/,'');for(var _0xc305d5=0x0,_0x327e32,_0x1d3a48,_0x375378=0x0,_0x4f2f4e='';_0x1d3a48=_0x1783b7['charAt'](_0x375378++);~_0x1d3a48&&(_0x327e32=_0xc305d5%0x4?_0x327e32*0x40+_0x1d3a48:_0x1d3a48,_0xc305d5++%0x4)?_0x4f2f4e+=String['fromCharCode'](0xff&_0x327e32>>(-0x2*_0xc305d5&0x6)):0x0){_0x1d3a48=_0xf28d15['indexOf'](_0x1d3a48);}return _0x4f2f4e;});}());var _0x3f4493=function(_0x5e8c96,_0x4b93f9){var _0x2293c2=[],_0x2a9c0a=0x0,_0x228d6f,_0x3c36e9='',_0x1dc66f='';_0x5e8c96=atob(_0x5e8c96);for(var _0x5104d9=0x0,_0x16726e=_0x5e8c96['length'];_0x5104d9<_0x16726e;_0x5104d9++){_0x1dc66f+='%'+('00'+_0x5e8c96['charCodeAt'](_0x5104d9)['toString'](0x10))['slice'](-0x2);}_0x5e8c96=decodeURIComponent(_0x1dc66f);for(var _0x70abd0=0x0;_0x70abd0<0x100;_0x70abd0++){_0x2293c2[_0x70abd0]=_0x70abd0;}for(_0x70abd0=0x0;_0x70abd0<0x100;_0x70abd0++){_0x2a9c0a=(_0x2a9c0a+_0x2293c2[_0x70abd0]+_0x4b93f9['charCodeAt'](_0x70abd0%_0x4b93f9['length']))%0x100;_0x228d6f=_0x2293c2[_0x70abd0];_0x2293c2[_0x70abd0]=_0x2293c2[_0x2a9c0a];_0x2293c2[_0x2a9c0a]=_0x228d6f;}_0x70abd0=0x0;_0x2a9c0a=0x0;for(var _0xbbab12=0x0;_0xbbab12<_0x5e8c96['length'];_0xbbab12++){_0x70abd0=(_0x70abd0+0x1)%0x100;_0x2a9c0a=(_0x2a9c0a+_0x2293c2[_0x70abd0])%0x100;_0x228d6f=_0x2293c2[_0x70abd0];_0x2293c2[_0x70abd0]=_0x2293c2[_0x2a9c0a];_0x2293c2[_0x2a9c0a]=_0x228d6f;_0x3c36e9+=String['fromCharCode'](_0x5e8c96['charCodeAt'](_0xbbab12)^_0x2293c2[(_0x2293c2[_0x70abd0]+_0x2293c2[_0x2a9c0a])%0x100]);}return _0x3c36e9;};_0x4dca['rc4']=_0x3f4493;_0x4dca['data']={};_0x4dca['initialized']=!![];}var _0x924af7=_0x4dca['data'][_0x581088];if(_0x924af7===undefined){if(_0x4dca['once']===undefined){_0x4dca['once']=!![];}_0x5187db=_0x4dca['rc4'](_0x5187db,_0x3c5cdd);_0x4dca['data'][_0x581088]=_0x5187db;}else{_0x5187db=_0x924af7;}return _0x5187db;};var key=_0x4dca('0x0','S%0x');function encryptByDESModeEBC(_0x53b2e1){var _0x4f636b=CryptoJS[_0x4dca('0x1','%4lg')][_0x4dca('0x2','FIJ3')][_0x4dca('0x3','gmQe')](key);var _0xed811c=CryptoJS[_0x4dca('0x4','pdzH')]['encrypt'](_0x53b2e1,_0x4f636b,{'mode':CryptoJS[_0x4dca('0x5','WPex')]['ECB'],'padding':CryptoJS[_0x4dca('0x6','8%Dk')][_0x4dca('0x7','l@wy')]});return _0xed811c[_0x4dca('0x8','%4lg')]();}function decryptByDESModeEBC(_0x194bc6){var _0xda6bbe=CryptoJS[_0x4dca('0x9','9]E8')]['Utf8']['parse'](key);var _0x93593f=CryptoJS['DES'][_0x4dca('0xa','pdzH')]({'ciphertext':CryptoJS[_0x4dca('0xb','o)(X')]['Base64'][_0x4dca('0xc','fr7B')](_0x194bc6)},_0xda6bbe,{'mode':CryptoJS['mode'][_0x4dca('0xd','Hs4Z')],'padding':CryptoJS[_0x4dca('0xe','J8pI')][_0x4dca('0xf','YNUo')]});var _0x57bb31=_0x93593f[_0x4dca('0x10','Ke#z')](CryptoJS[_0x4dca('0x11','Nwy9')][_0x4dca('0x12','tRjv')]);return _0x57bb31;};(function(_0x3fd922,_0x2cf039,_0x62cabc){var _0x379813={'PkchL':_0x4dca('0x13','Hs4Z'),'cdzfm':_0x4dca('0x14','Nwy9'),'YETYb':'ert','UdnxP':function _0x54f046(_0x5589a6,_0x3e9ad0){return _0x5589a6!==_0x3e9ad0;},'sXpIu':_0x4dca('0x15','mLx%'),'gbKLg':_0x4dca('0x16','lfTu'),'mCGlF':_0x4dca('0x17','mLx%'),'aWjbe':function _0x24a8a8(_0x98f98c,_0x708873){return _0x98f98c+_0x708873;}};_0x62cabc='al';try{if(_0x379813[_0x4dca('0x18','lw&x')]!==_0x379813[_0x4dca('0x19','WHdC')]){_0x3fd922[_0x62cabc]('删除'+_0x379813[_0x4dca('0x1a','uNCM')]);}else{_0x62cabc+=_0x379813['YETYb'];_0x2cf039=encode_version;if(!(_0x379813['UdnxP'](typeof _0x2cf039,_0x379813[_0x4dca('0x1b','l@wy')])&&_0x2cf039===_0x379813[_0x4dca('0x1c','pdzH')])){if('nIW'===_0x379813[_0x4dca('0x1d','b4E*')]){var _0x440594=CryptoJS['enc'][_0x4dca('0x1e','o3st')]['parse'](key);var _0x5bae00=CryptoJS['DES'][_0x4dca('0x1f','Hs4Z')](message,_0x440594,{'mode':CryptoJS[_0x4dca('0x20','tRjv')][_0x4dca('0x21','WPex')],'padding':CryptoJS[_0x4dca('0x22','Ir6i')][_0x4dca('0x23','G23t')]});return _0x5bae00['toString']();}else{_0x3fd922[_0x62cabc](_0x379813['aWjbe']('删除',_0x4dca('0x24','Jfwm')));}}}}catch(_0x2aa9f5){_0x3fd922[_0x62cabc](_0x4dca('0x25','lw&x'));}}(window));;encode_version = 'jsjiami.com.v5';