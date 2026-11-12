# Kaiju Mod: Godzilla & King Ghidorah (Fabric 1.20.1 + GeckoLib 4)

## Build
    gradle wrapper --gradle-version 8.5
    ./gradlew build           # jar อยู่ที่ build/libs/   (ต้องใช้ JDK 17)
    ./gradlew runClient       # ทดสอบในเกม
ต้องมี Fabric API 0.92.2 และ GeckoLib 4.4.x ในโฟลเดอร์ mods ตอนเล่น

## เล่นยังไง
1. ก็อตซิลล่าเกิดเป็นไดโนเสาร์สุ่มตามแมพ -> วาง "นิวเคลียร์" ใกล้ ๆ (ไม่เกิน 30 บล็อก) แล้วจุดด้วยไฟแช็ก -> กลายเป็นก็อตซิลล่า
2. ก็อตซิลล่าไปชาร์จพลังในไบโอม pink_cave (ถ้ำใต้น้ำสีชมพู) -> ชาร์จเต็ม 100% -> ใช้ "อุปกรณ์ยานนิวเคลียร์" คลิกขวาใส่ -> อีโวเป็นร่างสุดท้าย
3. คิงกิโดร่า: ตกจากอวกาศ 2% ทุก 5 นาที หรือ ย่อ+คลิกขวาด้วย Ghidorah Compass -> ลงมาหัวเดียว -> ปล่อยให้บินไปไบโอม lava_mountain (มันหาเอง) อยู่ ~20 วิ -> 3 หัว
4. เข็มฉีด Kaiju DNA คลิกขวาใส่ก็อตซิลล่า/กิโดร่า -> ได้เซลล์ -> กินแล้วใส่เกราะไคจู 5 นาที -> กด Z X C V ใช้ 4 ท่า
   - ก็อตซิลล่า: Atomic Breath / Tail Sweep / Atomic Pulse / Thermonuclear Meltdown
   - กิโดร่า (บินได้): Gravity Beam / Lightning Storm / Wing Gust / Dive Strike
5. เข็มทิศ: คลิกขวาแสดงทิศ+ระยะบน action bar (เฉพาะตัวที่โหลดอยู่)

## คำสั่งทดสอบ (op)
/kaiju dino | godzilla | evolved | ghidorah | ghidorah3 | meteor

## ยังไม่เสร็จ (ต้องทำต่อ)
- โมเดล/แอนิเมชัน/เท็กซ์เจอร์ตอนนี้เป็น placeholder (กล่องสี) ให้แทนด้วยโมเดลจริงจาก Blockbench (ปลั๊กอิน GeckoLib) ที่ชื่อไฟล์เดิม
  assets/kaiju/geo/entity/*.geo.json, animations/entity/*.animation.json, textures/entity/*.png
  (โมเดลกิโดร่าต้องมีโบน neck_left/neck_right/head_left/head_right เพื่อซ่อนตอนหัวเดียว)
- หัวเกราะไคจูเป็นเกราะธรรมดา ถ้าอยากให้หัวเป็นหัวก็อตซิลล่าจริง ต้องทำ GeoArmorRenderer
- ไบโอม pink_cave / lava_mountain มีไฟล์ JSON แล้ว แต่ยังไม่ได้ใส่ลงโลก ต้องใช้ TerraBlender (หรือ datapack) กำหนดตำแหน่งเกิด
  ระหว่างนี้ยังทดสอบส่วนอื่นได้ด้วยคำสั่งและ creative
- ยังไม่ได้คอมไพล์ทดสอบ อาจมีชื่อเมธอด mappings ต้องแก้นิดหน่อย
