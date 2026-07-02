package com.jeestudio.eco.soil.Enum;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 建设用地土壤污染风险管控标准 GB36600—2018<br/>
 * 标准文档：<a href="https://www.mee.gov.cn/ywgz/fgbz/bz/bzwb/trhj/201807/W020190626596188930731.pdf">点击这里</a>
 **/
/**
 * @Description: 建设用地土壤污染风险管控指标枚举
 */
public enum SoilConstructionlItem {
    ITEM_11ELYX("1,1-二氯乙烯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 66;
            } else {
                return 12;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 200;
            }else {
                return 40;
            }
        }
    },
    ITEM_11ELYW("1,1-二氯乙烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 9;
            } else {
                return 3;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 100;
            }else {
                return 20;
            }
        }
    },
    ITEM_SLYW_111("1,1,1-三氯乙烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 840;
            } else {
                return 701;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 840;
            }else {
                return 840;
            }
        }
    },
    ITEM_SLYW_1112_tr("1,1,1,2-四氯乙烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 10;
            } else {
                return 2.6;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 100;
            }else {
                return 26;
            }
        }
    },
    ITEM_SLYW_112("1,1,2-三氯乙烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 2.8;
            } else {
                return 0.6;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 15;
            }else {
                return 5;
            }
        }
    },
    ITEM_SLYW_1122_tr("1,1,2,2-四氯乙烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 6.8;
            } else {
                return 1.6;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 50;
            }else {
                return 14;
            }
        }
    },
    ITEM_ELBW_12("1,2-二氯丙烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 5;
            } else {
                return 1;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 47;
            }else {
                return 5;
            }
        }
    },
    ITEM_12ELYW("1,2-二氯乙烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 5;
            } else {
                return 0.52;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 21;
            }else {
                return 6;
            }
        }
    },
    ITEM_ELB_12("1,2-二氯苯","邻二氯苯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 560;
            } else {
                return 560;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 560;
            }else {
                return 560;
            }
        }
    },
    ITEM_EXYW_12_tr("1,2-二溴乙烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.24;
            } else {
                return 0.07;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 2.4;
            }else {
                return 0.7;
            }
        }
    },
    ITEM_SLPW_123("1,2,3-三氯丙烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.5;
            } else {
                return 0.05;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 5;
            }else {
                return 0.5;
            }
        }
    },
    ITEM_ELB_14("1,4-二氯苯","对二氯苯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 20;
            } else {
                return 5.6;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 200;
            }else {
                return 56;
            }
        }
    },
    ITEM_LF_2("2-氯酚"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 2256;
            } else {
                return 250;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 4500;
            }else {
                return 500;
            }
        }
    },
    ITEM_ELF_24("2,4-二氯酚"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 843;
            } else {
                return 117;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1690;
            }else {
                return 234;
            }
        }
    },
    ITEM_EXJJB_24("2,4-二硝基甲苯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 5.2;
            } else {
                return 1.8;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 52;
            }else {
                return 18;
            }
        }
    },
    ITEM_EXJF_24_tr("2,4-二硝基酚"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 562;
            } else {
                return 78;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1130;
            }else {
                return 156;
            }
        }
    },
    ITEM_SLF_246("2,4,6-三氯酚"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 137;
            } else {
                return 39;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 560;
            }else {
                return 78;
            }
        }
    },
    ITEM_ELLBA_33("3,3-二氯联苯胺","3,3'-二氯联苯胺"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 3.6;
            } else {
                return 1.3;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 36;
            }else {
                return 13;
            }
        }
    },
    ITEM_WLLB_33445("3,3',4,4',5-五氯联苯","3,3',4,4',5-五氯联苯（PCB 126）"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.0001;
            } else {
                return 0.00004;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.001;
            }else {
                return 0.0004;
            }
        }
    },
    ITEM_LVLB_334455("3,3',4,4',5,5'-六氯联苯","3,3',4,4',5,5'-六氯联苯（PCB 169）"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.0004;
            } else {
                return 0.0001;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.004;
            }else {
                return 0.001;
            }
        }
    },
    ITEM_DDY_pp("pp-DDE","p,p'-滴滴伊"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 7;
            } else {
                return 2;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 70;
            }else {
                return 20;
            }
        }
    },
    ITEM_DDD_pp("pp-DDD","p,p'-滴滴滴"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 7.1;
            } else {
                return 2.5;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 71;
            }else {
                return 25;
            }
        }
    },
    ITEM_LLL_A("α-六六六"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.3;
            } else {
                return 0.09;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 3;
            }else {
                return 0.9;
            }
        }
    },
    ITEM_LLL_B("β-六六六"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.92;
            } else {
                return 0.32;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 9.2;
            }else {
                return 3.2;
            }
        }
    },
    ITEM_LLL_Y("γ-六六六"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1.9;
            } else {
                return 0.62;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 19;
            }else {
                return 6.2;
            }
        }
    },
    ITEM_YXELJW("一溴二氯甲烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1.2;
            } else {
                return 0.29;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 12;
            }else {
                return 2.9;
            }
        }
    },
    ITEM_QILV("七氯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.37;
            } else {
                return 0.13;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 3.7;
            }else {
                return 1.3;
            }
        }
    },
    ITEM_SLYX("三氯乙烯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 2.8;
            } else {
                return 0.7;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 20;
            }else {
                return 7;
            }
        }
    },
    ITEM_LG("乐果"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 619;
            } else {
                return 86;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1240;
            }else {
                return 170;
            }
        }
    },
    ITEM_YB("乙苯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 28;
            } else {
                return 7.2;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 280;
            }else {
                return 72;
            }
        }
    },
    /**
     * 二噁英 标准中单位为 mg/kg，实际检测过程中，单位为 ng/kg sheji
     **/
    ITEM_EEYL("二噁英类","二噁英类（总毒性当量）","二噁英"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 40;
            } else {
                return 10;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 400;
            }else {
                return 100;
            }
        }
    },
    ITEM_ELJW("二氯甲烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 616;
            } else {
                return 94;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 2000;
            }else {
                return 300;
            }
        }
    },
    ITEM_EXLJW_tr("二溴氯甲烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 33;
            } else {
                return 9.3;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 330;
            }else {
                return 93;
            }
        }
    },
    ITEM_EBBN_AH("二苯并(a,h)蒽","二苯并[a,h]蒽"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1.5;
            } else {
                return 0.55;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 15;
            }else {
                return 5.5;
            }
        }
    },
    ITEM_WLF("五氯酚"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 2.7;
            } else {
                return 1.1;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 27;
            }else {
                return 12;
            }
        }
    },
    ITEM_LCHWDX("六氯环戊二烯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 5.2;
            } else {
                return 1.1;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 10;
            }else {
                return 2.3;
            }
        }
    },
    ITEM_LLB("六氯苯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1;
            } else {
                return 0.33;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 10;
            }else {
                return 3.3;
            }
        }
    },
    ITEM_FELYX_12_tr("反-1,2-二氯乙烯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 54;
            } else {
                return 10;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 163;
            }else {
                return 31;
            }
        }
    },
    ITEM_SILVYIXI("四氯乙烯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 53;
            } else {
                return 11;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 183;
            }else {
                return 34;
            }
        }
    },
    ITEM_SLHT("四氯化碳"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 2.8;
            } else {
                return 0.9;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 36;
            }else {
                return 9;
            }
        }
    },
    ITEM_BLLBZL("多氯联苯（总量）"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.38;
            } else {
                return 0.14;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 3.8;
            }else {
                return 1.4;
            }
        }
    },
    ITEM_DXLBZL("多溴联苯（总量）"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.06;
            } else {
                return 0.02;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.6;
            }else {
                return 0.2;
            }
        }
    },
    ITEM_DDW("敌敌畏"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 5;
            } else {
                return 1.8;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 50;
            }else {
                return 18;
            }
        }
    },
    ITEM_LVDAN("氯丹"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 6.2;
            } else {
                return 2;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 62;
            }else {
                return 20;
            }
        }
    },
    ITEM_LYX("氯乙烯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.43;
            } else {
                return 0.12;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 4.3;
            }else {
                return 1.2;
            }
        }
    },
    ITEM_LF("氯仿"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.9;
            } else {
                return 0.3;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 10;
            }else {
                return 5;
            }
        }
    },
    ITEM_LVJIAWAN("氯甲烷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 37;
            } else {
                return 12;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 120;
            }else {
                return 21;
            }
        }
    },
    ITEM_LB("氯苯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 270;
            } else {
                return 68;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1000;
            }else {
                return 200;
            }
        }
    },
    ITEM_QHW("氰化物"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 135;
            } else {
                return 22;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 270;
            }else {
                return 44;
            }
        }
    },
    ITEM_GONG("汞"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 38;
            } else {
                return 8;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 82;
            }else {
                return 33;
            }
        }
    },
    ITEM_XF("溴仿"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 103;
            } else {
                return 32;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1030;
            }else {
                return 320;
            }
        }
    },
    ITEM_DDT("滴滴涕","滴滴涕总量"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 6.7;
            } else {
                return 2;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 67;
            }else {
                return 21;
            }
        }
    },
    ITEM_MYL("灭蚁灵"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.09;
            } else {
                return 0.03;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 0.9;
            }else {
                return 0.3;
            }
        }
    },
    ITEM_JJG_tr("甲基汞"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 45;
            } else {
                return 5;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 120;
            }else {
                return 10;
            }
        }
    },
    ITEM_MB("甲苯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1200;
            } else {
                return 1200;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1200;
            }else {
                return 1200;
            }
        }
    },
    ITEM_SYT("石油烃（C10-C40）"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 4500;
            } else {
                return 826;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 9000;
            }else {
                return 5000;
            }
        }
    },
    ITEM_SEN("砷"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 60;
            } else {
                return 20;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 140;
            }else {
                return 120;
            }
        }
    },
    ITEM_XJB("硝基苯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 76;
            } else {
                return 34;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 760;
            }else {
                return 190;
            }
        }
    },
    ITEM_LIUDAN("硫丹"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1687;
            } else {
                return 234;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 3400;
            }else {
                return 470;
            }
        }
    },
    ITEM_BEN("苯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 4;
            } else {
                return 1;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 40;
            }else {
                return 10;
            }
        }
    },
    ITEM_BYX("苯乙烯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1290;
            } else {
                return 1290;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1290;
            }else {
                return 1290;
            }
        }
    },
    ITEM_BENBINGBI("苯并(a)芘","苯并a芘","苯并[a]芘"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1.5;
            } else {
                return 0.55;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 15;
            }else {
                return 5.5;
            }
        }
    },
    ITEM_BBN_A("苯并(a)蒽","苯并[a]蒽"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 15;
            } else {
                return 5.5;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 151;
            }else {
                return 55;
            }
        }
    },
    ITEM_BBYN_B("苯并(b)荧蒽","苯并[b]荧蒽"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 15;
            } else {
                return 5.5;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 151;
            }else {
                return 55;
            }
        }
    },
    ITEM_BBYN_K("苯并(k)荧蒽","苯并[k]荧蒽"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 151;
            } else {
                return 55;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1500;
            }else {
                return 550;
            }
        }
    },
    ITEM_BENAN_tr("苯胺"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 260;
            } else {
                return 92;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 663;
            }else {
                return 211;
            }
        }
    },
    ITEM_BBB_123("茚并(1,2,3-cd)芘","茚并[1,2,3-cd]芘"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 15;
            } else {
                return 5.5;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 151;
            }else {
                return 55;
            }
        }
    },
    ITEM_NAI("萘"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 70;
            } else {
                return 25;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 700;
            }else {
                return 255;
            }
        }
    },
    ITEM_LDMB("邻二甲苯","邻-二甲苯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 640;
            } else {
                return 222;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 640;
            }else {
                return 640;
            }
        }
    },
    ITEM_LBEJSDJBZ("邻苯二甲酸丁基苄酯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 900;
            } else {
                return 312;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 9000;
            }else {
                return 3120;
            }
        }
    },
    ITEM_LBEJSEZ_2YJJJ("邻苯二甲酸二(2-乙基己基)酯","邻苯二甲酸二(2-乙基己\n基)酯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 121;
            } else {
                return 42;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1210;
            }else {
                return 420;
            }
        }
    },
    ITEM_LBEJSEZXZ_tr("邻苯二甲酸二正辛酯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 2812;
            } else {
                return 390;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 5700;
            }else {
                return 800;
            }
        }
    },
    ITEM_FAN_tr("钒"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 752;
            } else {
                return 165;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1500;
            }else {
                return 330;
            }
        }
    },
    ITEM_GU("钴"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 70;
            } else {
                return 20;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 350;
            }else {
                return 190;
            }
        }
    },
    ITEM_QIAN("铅"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 800;
            } else {
                return 400;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 2500;
            }else {
                return 800;
            }
        }
    },
    ITEM_PI("铍"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 29;
            } else {
                return 15;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 290;
            }else {
                return 98;
            }
        }
    },
    ITEM_TONG("铜"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 18000;
            } else {
                return 2000;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 36000;
            }else {
                return 8000;
            }
        }
    },
    ITEM_LIUJIAGE("铬（六价）"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 5.7;
            } else {
                return 3;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 78;
            }else {
                return 30;
            }
        }
    },
    ITEM_DI("锑"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 180;
            } else {
                return 20;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 360;
            }else {
                return 40;
            }
        }
    },
    ITEM_GE_tr("镉"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 65;
            } else {
                return 20;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 172;
            }else {
                return 47;
            }
        }
    },
    ITEM_NIE("镍"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 900;
            } else {
                return 150;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 2000;
            }else {
                return 600;
            }
        }
    },
    ITEM_JDMB_DDMB("间二甲苯+对二甲苯","间-二甲苯+对-二甲苯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 570;
            } else {
                return 163;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 570;
            }else {
                return 500;
            }
        }
    },
    ITME_ATLJ("阿特拉津"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 7.4;
            } else {
                return 2.6;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 74;
            }else {
                return 26;
            }
        }
    },
    ITEM_SELYX_12_tr("顺-1,2-二氯乙烯"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 596;
            } else {
                return 66;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 2000;
            }else {
                return 200;
            }
        }
    },
    ITEM_QU_tr("䓛"){

        @Override
        public double getScreeningValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 1293;
            } else {
                return 490;
            }
        }

        @Override
        public double getControlValue(SoilUseType soilUseType) {
            if (SoilUseType.TheSecondTypeOfLandUse == soilUseType) {
                return 12900;
            }else {
                return 4900;
            }
        }
    };

    private final String itemName;
    private final List<String> otherName = new ArrayList<>();

    SoilConstructionlItem(String itemName) {
        this.itemName = itemName;
    }
    SoilConstructionlItem(String itemName, String ...otherName) {
        this.itemName = itemName;
        this.otherName.addAll(Arrays.asList(otherName));
    }

    public String getItemName() {
        return itemName;
    }
    public List<String> getOtherName() {
        return otherName;
    }

    public static SoilConstructionlItem fromString(String itemName) {
        if (StringUtils.isEmpty(itemName)) {
            return null;
        }
        for (SoilConstructionlItem item : values()) {
            if (item.itemName.equals(itemName) || item.otherName.contains(itemName)) {
                return item;
            }
        }
        return null;
    }
    // 获取筛选值
    public abstract double getScreeningValue(SoilUseType soilUseType);
    // 获取管制值
    public abstract double getControlValue(SoilUseType soilUseType);

}
