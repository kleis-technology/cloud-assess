# Trusted library

Parameters and required data
- timewindow
- maintenance intensity
- datacenter
  - inventory: the characteristics of each datacenter
    - total power
    - reserved power
    - lifespan
    - pue
  - impacts
- electricity mix
  - impacts per geography
- network equipments
  - inventory: the characteristics of each network equipment reference
    - cf below
  - impacts: for each network equipment
    - the 20 indicators
- servers (physical)
  - inventory: the characteristics of each server reference
      - cf below
  - impacts
      - the 20 indicators
- storage servers
  - inventory: the characteristics of each storage server reference
      - cf below
  - impacts
      - the 20 indicators

For each equipment (server, network or storage)
- datacenter id
- pool id
  - equipments can be organized as pools
  - usually used to segregate equipments for various services
  - /!\ special pool mutualized
- power
- lifespan
- quantity

Indicators
- ADPe (kg_Sb_Eq)
- ADPf (MJ_net_calorific_value)
- AP (mol_H_p_Eq)
- CTUe (CTUe)
- CTUh_c (CTUh)
- CTUh_nc (CTUh)
- Epf (kg_P_Eq)
- Epm (kg_N_Eq)
- Ept (mol_N_Eq)
- GWP (kg_CO2_Eq)
- GWPb (kg_CO2_Eq)
- GWPf (kg_CO2_Eq)
- GWPlu (kg_CO2_Eq)
- IR (kBq_U235_Eq)
- LU (u)
- ODP (kg_CFC_11_Eq)
- PM (disease_incidence)
- POCP (kg_NMVOC_Eq)
- WU (m3_world_eq_deprived)
- TPE (MJ_net_calorific_value)
