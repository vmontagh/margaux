open util/integer

/*
Decisions:
1. covering material
2. sterilization method
3. type of sensor
4. thickness of covering material
*/

one sig Design {
	material : one Material,
	thickness : one Thickness, 
	sensor : one Sensor,
	density : one Density,
	sterilization : one Sterilization,

	--Metric placed here because we currently cannot call div from objectives block
	sensorResolution: one Int
}{
	sensorResolution = div[sensor.resolution[density], material.gain[thickness]]
}

sig Material {
	gain: Thickness -> Int,
	washes: Sterilization -> Int
}

sig Sensor {
	resolution: Density -> Int,  -- resolution is different at different densities
	cost: Density -> Int
}

sig Density, Thickness, Sterilization {}

inst designInst {
	10 Int,
	Material = Silicon + PVDF + Elastomer,
	Thickness = point1mm + point2mm + point4mm + point6mm,
	Sterilization = Steam + Gas + Gamma,
	Sensor = Capacitive + PiezoElectric + PiezoResistive + FET,
	Density = Full + ThreeQuarters + Half,
	
	--Material: gain and washes
	gain = Silicon->point1mm->96 + Silicon->point2mm->89 + Silicon->point4mm->66 + Silicon->point6mm->42 +
				PVDF->point1mm->83 +  PVDF->point2mm->59 + PVDF->point4mm->15 + PVDF->point6mm->2 	+
				Elastomer->point1mm->94 + Elastomer->point2mm->85 + Elastomer->point4mm->56 + Elastomer->point6mm->30,
	washes = Silicon->Steam->25 + Silicon->Gas->100 + Silicon->Gamma->0 +
					PVDF->Steam->25 +	PVDF->Gas->5 + PVDF->Gamma->6 +
					Elastomer->Steam->100 + Elastomer->Gas->5 + Elastomer->Gamma->3,

	--Sensor: resolution and cost
	resolution = Capacitive->Full->24 + Capacitive->ThreeQuarters->28 + Capacitive->Half->34 +
						PiezoElectric->Full->100 + PiezoElectric->ThreeQuarters->115 + PiezoElectric->Half->141 +
						PiezoResistive->Full->125 + PiezoResistive->ThreeQuarters->144 + PiezoResistive->Half->177 +
						FET->Full->100 + FET->ThreeQuarters->115 + FET->Half->141,  --1mm is 100 units here, add smaller increments
	cost = Capacitive->Full->300 + Capacitive->ThreeQuarters->225 + Capacitive->Half->150 +
				PiezoElectric->Full->150 + PiezoElectric->ThreeQuarters->113 + PiezoElectric->Half->75 +
				PiezoResistive->Full->200 + PiezoResistive->ThreeQuarters->150 + PiezoResistive->Half->10 +
				FET->Full->400 + FET->ThreeQuarters->300 + FET->Half->200 --cost facor time 100
}

objectives m {
	-- reusability (total number of washes)
	-- minimize to search for disposable configuration
	maximize Design.material.washes[Design.sterilization],
	-- cost
	minimize Design.sensor.cost[Design.density],
	-- resolution
	minimize Design.sensorResolution
	--minimize Design.sensor.resolution[Design.density] / Design.material.gain[Design.thickness]
}

pred show {}

run show for designInst optimize m
