package com.jeestudio.eco.soil.Enum;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 农用地土壤污染风险管控标准 GB 15618-2018<br/>
 * 标准文档：<a href="https://www.mee.gov.cn/ywgz/fgbz/bz/bzwb/trhj/201807/W020190626595212456114.pdf">点击这里</a>
 **/
/**
 * @Description: 农用地土壤污染风险管控指标枚举
 */
public enum SoilAgriculturalItem {
    CADMIUM("镉") {
        @Override
        public double getScreeningValue(double ph, SoilUseType soilUseType) {
            if (ph <= 5.5) {
                return 0.3;
            } else if (ph <= 6.5) {
                return soilUseType.isPaddyField() ? 0.4 : 0.3;
            } else if (ph <= 7.5) {
                return soilUseType.isPaddyField()  ? 0.6 : 0.3;
            } else {
                return soilUseType.isPaddyField()  ? 0.8 : 0.6;
            }
        }

        @Override
        public Double getControlValue(double ph) {
            if (ph <= 5.5) {
                return 1.5;
            } else if (ph <= 6.5) {
                return 2.0;
            } else if (ph <= 7.5) {
                return 3.0;
            } else {
                return 4.0;
            }
        }
    },
    MERCURY("汞") {
        @Override
        public double getScreeningValue(double ph, SoilUseType soilUseType) {
            if (ph <= 5.5) {
                return soilUseType.isPaddyField()  ? 0.5 : 1.3;
            } else if (ph <= 6.5) {
                return soilUseType.isPaddyField()  ? 0.5 : 1.8;
            } else if (ph <= 7.5) {
                return soilUseType.isPaddyField()  ? 0.6 : 2.4;
            } else {
                return soilUseType.isPaddyField()  ? 1.0 : 3.4;
            }
        }

        @Override
        public Double getControlValue(double ph) {
            if (ph <= 5.5) {
                return 2.0;
            } else if (ph <= 6.5) {
                return 2.5;
            } else if (ph <= 7.5) {
                return 4.0;
            } else {
                return 6.0;
            }
        }
    },
    ARSENIC("砷") {
        @Override
        public double getScreeningValue(double ph, SoilUseType soilUseType) {
            if (ph <= 6.5) {
                return soilUseType.isPaddyField()  ? 30 : 40;
            } else if (ph <= 7.5) {
                return soilUseType.isPaddyField()  ? 25 : 30;
            } else {
                return soilUseType.isPaddyField()  ? 20 : 25;
            }
        }

        @Override
        public Double getControlValue(double ph) {
            if (ph <= 5.5) {
                return 200.0;
            } else if (ph <= 6.5) {
                return 150.0;
            } else if (ph <= 7.5) {
                return 120.0;
            } else {
                return 100.0;
            }
        }
    },
    LEAD("铅") {
        @Override
        public double getScreeningValue(double ph, SoilUseType soilUseType) {
            if (ph <= 5.5) {
                return soilUseType.isPaddyField()  ? 80 : 70;
            } else if (ph <= 6.5) {
                return soilUseType.isPaddyField()  ? 100 : 90;
            } else if (ph <= 7.5) {
                return soilUseType.isPaddyField()  ? 140 : 120;
            } else {
                return soilUseType.isPaddyField()  ? 240 : 170;
            }
        }

        @Override
        public Double getControlValue(double ph) {
            if (ph <= 5.5) {
                return 400.0;
            } else if (ph <= 6.5) {
                return 500.0;
            } else if (ph <= 7.5) {
                return 700.0;
            } else {
                return 1000.0;
            }
        }
    },
    CHROMIUM("铬") {
        @Override
        public double getScreeningValue(double ph, SoilUseType soilUseType) {
            if (ph <= 5.5) {
                return soilUseType.isPaddyField() ? 250 : 150;
            } else if (ph <= 6.5) {
                return soilUseType.isPaddyField()  ? 250 : 150;
            } else if (ph <= 7.5) {
                return soilUseType.isPaddyField()  ? 300 : 200;
            } else {
                return soilUseType.isPaddyField()  ? 350 : 250;
            }
        }

        @Override
        public Double getControlValue(double ph) {
            if (ph <= 5.5) {
                return 800.0;
            } else if (ph <= 6.5) {
                return 850.0;
            } else if (ph <= 7.5) {
                return 1000.0;
            } else {
                return 1300.0;
            }
        }
    },
    COPPER("铜") {
        @Override
        public double getScreeningValue(double ph, SoilUseType soilUseType) {
            return ph <= 6.5
                ? (soilUseType.isOrchard()? 150 : 50)
                : (soilUseType.isOrchard() ? 200 : 100);
        }

        @Override
        public Double getControlValue(double ph) {
            return null;
        }
    },
    NICKEL("镍") {
        @Override
        public double getScreeningValue(double ph, SoilUseType soilUseType) {
            if (ph <= 5.5) {
                return 60;
            } else if (ph <= 6.5) {
                return 70;
            } else if (ph <= 7.5) {
                return 100;
            } else {
                return 190;
            }
        }

        @Override
        public Double getControlValue(double ph) {
            return null;
        }
    },
    ZINC("锌") {
        @Override
        public double getScreeningValue(double ph, SoilUseType soilUseType) {
            if (ph <= 5.5) {
                return 200;
            } else if (ph <= 6.5) {
                return 200;
            } else if (ph <= 7.5) {
                return 250;
            } else {
                return 300;
            }
        }

        @Override
        public Double getControlValue(double ph) {
            return null;
        }
    },
    BHC("六六六","六六六总量") {
        @Override
        public double getScreeningValue(double ph, SoilUseType soilUseType) {
            return 0.10;
        }

        @Override
        public Double getControlValue(double ph) {
            return null;
        }
    },
    DDT("滴滴涕","滴滴涕总量") {
        @Override
        public double getScreeningValue(double ph, SoilUseType soilUseType) {
            return 0.10;
        }

        @Override
        public Double getControlValue(double ph) {
            return null;
        }
    },
    BENZOAPYRENE("苯并(a)芘","苯并a芘","苯并[a]芘") {
        @Override
        public double getScreeningValue(double ph, SoilUseType soilUseType) {
            return 0.55;
        }

        @Override
        public Double getControlValue(double ph) {
            return null;
        }
    };


    private final String itemName;
    private final List<String> otherName = new ArrayList<>();

    SoilAgriculturalItem(String itemName) {
        this.itemName = itemName;
    }
    SoilAgriculturalItem(String itemName, String ...otherName) {
        this.itemName = itemName;
        this.otherName.addAll(Arrays.asList(otherName));
    }

    public String getItemName() {
        return itemName;
    }
    public List<String> getOtherName() {
        return otherName;
    }

    public static SoilAgriculturalItem fromString(String itemName) {
        if (StringUtils.isEmpty(itemName)) {
            return null;
        }
        for (SoilAgriculturalItem item : values()) {
            if (item.itemName.equals(itemName) || item.otherName.contains(itemName)) {
                return item;
            }
        }
        return null;
    }
    // 获取筛选值
    public abstract double getScreeningValue(double ph, SoilUseType soilUseType);
    // 获取管制值
    public abstract Double getControlValue(double ph);

}
