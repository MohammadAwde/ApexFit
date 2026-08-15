package com.example.data.model

data class RecipeIngredient(
    val item: String,
    val amount: String,
    val icon: String = "🥗"
)

data class RecipeStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val durationMinutes: Int = 0,
    val proTip: String? = null
)

data class FitnessRecipe(
    val id: String,
    val title: String,
    val category: String, // Breakfast, Lunch, Dinner, Snack, Post-Workout
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val difficulty: String, // Easy, Intermediate, Advanced
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val fiberG: Int,
    val description: String,
    val tags: List<String>,
    val ingredients: List<RecipeIngredient>,
    val steps: List<RecipeStep>,
    val chefTip: String
)

object FitnessRecipeCatalog {
    val ALL_RECIPES: List<FitnessRecipe> by lazy { recipes }

    val recipes = listOf(
        FitnessRecipe(
            id = "rec_salmon_bowl",
            title = "Mediterranean Grilled Salmon & Quinoa Bowl",
            category = "Dinner",
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 1,
            difficulty = "Easy",
            calories = 540,
            proteinG = 46,
            carbsG = 42,
            fatG = 20,
            fiberG = 7,
            description = "Crispy pan-seared wild salmon over fluffy lemon-herb quinoa, cucumber, cherry tomatoes, kalamata olives, and creamy garlic tzatziki.",
            tags = listOf("High Protein", "Omega-3", "Heart Healthy", "Gluten-Free"),
            ingredients = listOf(
                RecipeIngredient("Wild Atlantic Salmon Fillet", "200g", "🐟"),
                RecipeIngredient("Cooked Tricolor Quinoa", "150g (1 cup)", "🌾"),
                RecipeIngredient("English Cucumber (diced)", "1/2 cup", "🥒"),
                RecipeIngredient("Cherry Tomatoes (halved)", "1/2 cup", "🍅"),
                RecipeIngredient("Greek Tzatziki or 0% Greek Yogurt", "2 tbsp (30g)", "🥣"),
                RecipeIngredient("Extra Virgin Olive Oil", "1 tsp (5ml)", "🫒"),
                RecipeIngredient("Fresh Lemon Juice & Dried Oregano", "1 tbsp lemon + 1/2 tsp oregano", "🍋"),
                RecipeIngredient("Sea Salt & Coarse Black Pepper", "To taste", "🧂")
            ),
            steps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    title = "Season the Salmon",
                    description = "Pat the salmon fillet dry with a paper towel. Rub both sides with 1 tsp olive oil, sea salt, black pepper, and dried oregano.",
                    durationMinutes = 2,
                    proTip = "Dry salmon skin ensures an ultra-crispy crust when searing."
                ),
                RecipeStep(
                    stepNumber = 2,
                    title = "Sear Salmon to Perfection",
                    description = "Heat a non-stick or cast-iron skillet over medium-high heat. Place salmon skin-side down and press gently with spatula. Sear for 4-5 minutes until skin is golden and crispy, flip and cook for another 3-4 minutes until medium-rare to medium.",
                    durationMinutes = 8,
                    proTip = "Avoid moving the salmon for the first 3 minutes so it releases cleanly from the pan."
                ),
                RecipeStep(
                    stepNumber = 3,
                    title = "Warm and Season Quinoa",
                    description = "In a bowl, toss warm cooked quinoa with fresh lemon juice, chopped fresh parsley, and a pinch of salt.",
                    durationMinutes = 2
                ),
                RecipeStep(
                    stepNumber = 4,
                    title = "Assemble the Power Bowl",
                    description = "Layer the lemon quinoa as the base. Top with the seared salmon fillet, diced cucumber, cherry tomatoes, and a dollop of creamy garlic tzatziki. Garnish with a lemon wedge.",
                    durationMinutes = 3,
                    proTip = "Sprinkle sumac or toasted pine nuts on top for an extra Mediterranean crunch."
                )
            ),
            chefTip = "Wild sockeye or coho salmon provides superior omega-3 fatty acids for muscle recovery and joint lubrication."
        ),
        FitnessRecipe(
            id = "rec_chicken_sweet_potato",
            title = "Herb-Crusted Chicken Breast & Roasted Sweet Potatoes",
            category = "Lunch",
            prepTimeMinutes = 12,
            cookTimeMinutes = 22,
            servings = 1,
            difficulty = "Easy",
            calories = 510,
            proteinG = 52,
            carbsG = 48,
            fatG = 12,
            fiberG = 6,
            description = "Juicy rosemary-garlic chicken breast paired with caramelized roasted sweet potato cubes and steamed crisp broccoli florets.",
            tags = listOf("Clean Bulk", "High Protein", "Low Fat", "Meal Prep Favorite"),
            ingredients = listOf(
                RecipeIngredient("Skinless Boneless Chicken Breast", "220g", "🍗"),
                RecipeIngredient("Sweet Potato (cubed)", "200g (1 medium)", "🍠"),
                RecipeIngredient("Fresh Broccoli Florets", "150g (1.5 cups)", "🥦"),
                RecipeIngredient("Olive Oil Spray", "2-3 sprays (3ml)", "🫒"),
                RecipeIngredient("Smoked Paprika & Garlic Powder", "1 tsp paprika + 1/2 tsp garlic", "🧄"),
                RecipeIngredient("Fresh Rosemary or Thyme", "1 sprig chopped", "🌿"),
                RecipeIngredient("Himalayan Pink Salt & Black Pepper", "To taste", "🧂")
            ),
            steps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    title = "Preheat & Roast Sweet Potatoes",
                    description = "Preheat oven to 200°C (400°F). Toss cubed sweet potatoes with light olive oil spray, smoked paprika, sea salt, and black pepper. Spread on a baking sheet and roast for 20-22 minutes until edges are caramelized and fork-tender.",
                    durationMinutes = 20,
                    proTip = "Cut sweet potato cubes evenly (1.5cm) for uniform baking."
                ),
                RecipeStep(
                    stepNumber = 2,
                    title = "Prep and Butterfly Chicken",
                    description = "Evenly flatten chicken breast or slice horizontally. Season generously with minced rosemary, garlic powder, paprika, salt, and pepper.",
                    durationMinutes = 3
                ),
                RecipeStep(
                    stepNumber = 3,
                    title = "Pan-Sear Chicken",
                    description = "Heat a skillet over medium heat. Sear chicken breast for 5-6 minutes per side until golden brown and internal temperature reaches 74°C (165°F). Let rest on a cutting board for 4 minutes before slicing.",
                    durationMinutes = 12,
                    proTip = "Resting the chicken preserves all the internal juices, preventing dry meat."
                ),
                RecipeStep(
                    stepNumber = 4,
                    title = "Steam Broccoli & Plate",
                    description = "Steam broccoli florets in a covered pot with 2 tbsp water for 3-4 minutes until bright emerald green. Plate sliced chicken alongside roasted sweet potatoes and broccoli.",
                    durationMinutes = 4
                )
            ),
            chefTip = "Prepare 3-4 batches on Sunday for the ultimate grab-and-go anabolic lunch box."
        ),
        FitnessRecipe(
            id = "rec_eggs_avocado_toast",
            title = "Golden Soft-Scrambled Eggs & Avocado Sourdough",
            category = "Breakfast",
            prepTimeMinutes = 5,
            cookTimeMinutes = 8,
            servings = 1,
            difficulty = "Easy",
            calories = 440,
            proteinG = 26,
            carbsG = 34,
            fatG = 22,
            fiberG = 8,
            description = "Velvety soft-scrambled pasture-raised eggs served over warm toasted artisan sourdough bread with mashed Hass avocado and red chili flakes.",
            tags = listOf("Quick Prep", "Brain Fuel", "Vegetarian", "Choline Rich"),
            ingredients = listOf(
                RecipeIngredient("Pasture-Raised Whole Eggs", "3 large eggs", "🥚"),
                RecipeIngredient("Artisan Sourdough Bread", "1 thick slice (50g)", "🍞"),
                RecipeIngredient("Ripe Hass Avocado", "1/2 medium (75g)", "🥑"),
                RecipeIngredient("Organic Butter or Ghee", "1 tsp (5g)", "🧈"),
                RecipeIngredient("Fresh Chives (finely snipped)", "1 tbsp", "🌱"),
                RecipeIngredient("Red Pepper Chili Flakes", "1/4 tsp", "🌶️"),
                RecipeIngredient("Maldon Flaky Sea Salt", "To taste", "🧂")
            ),
            steps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    title = "Prep the Avocado Base",
                    description = "Mash half an avocado in a small bowl with a fork, a squeeze of fresh lemon juice, flaky sea salt, and black pepper.",
                    durationMinutes = 2
                ),
                RecipeStep(
                    stepNumber = 2,
                    title = "Toast the Sourdough",
                    description = "Toast the sourdough slice until golden and crunchy. Spread the mashed avocado evenly across the surface.",
                    durationMinutes = 3,
                    proTip = "A sturdy sourdough crust holds up best under soft eggs."
                ),
                RecipeStep(
                    stepNumber = 3,
                    title = "Gently Scramble the Eggs",
                    description = "Whisk eggs with a pinch of salt until frothy. Melt butter in a non-stick pan over LOW heat. Pour in eggs and use a silicone spatula to gently push curds from edges to center in slow waves for 2-3 minutes. Remove from heat while still glossy.",
                    durationMinutes = 4,
                    proTip = "Low heat and patience produce luxurious, creamy restaurant-quality curds."
                ),
                RecipeStep(
                    stepNumber = 4,
                    title = "Garnish & Serve",
                    description = "Spoon velvety eggs over the avocado toast. Top with snipped chives, red chili flakes, and flaky sea salt.",
                    durationMinutes = 1
                )
            ),
            chefTip = "Egg yolks are nature's richest source of choline, essential for cognitive focus and motor unit recruitment during workouts."
        ),
        FitnessRecipe(
            id = "rec_overnight_oats",
            title = "Anabolic Dark Chocolate Peanut Butter Overnight Oats",
            category = "Breakfast",
            prepTimeMinutes = 6,
            cookTimeMinutes = 0,
            servings = 1,
            difficulty = "Easy",
            calories = 460,
            proteinG = 38,
            carbsG = 48,
            fatG = 14,
            fiberG = 9,
            description = "High-protein rolled oats soaked overnight in almond milk, whey protein isolate, raw cacao powder, chia seeds, and all-natural peanut butter.",
            tags = listOf("Zero Cook", "High Protein", "Slow-Release Carbs", "Meal Prep"),
            ingredients = listOf(
                RecipeIngredient("Rolled Old Fashioned Oats", "50g (1/2 cup)", "🌾"),
                RecipeIngredient("Chocolate Whey Isolate Protein", "1 scoop (30g)", "🍫"),
                RecipeIngredient("Unsweetened Almond Milk", "180ml (3/4 cup)", "🥛"),
                RecipeIngredient("Natural Creamy Peanut Butter", "1 tbsp (16g)", "🥜"),
                RecipeIngredient("Organic Chia Seeds", "1 tbsp (10g)", "🌱"),
                RecipeIngredient("Raw Cacao Powder", "1 tsp (5g)", "☕"),
                RecipeIngredient("Sliced Banana / Dark Choc Chips", "1/2 banana + 5g chips", "🍌")
            ),
            steps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    title = "Mix Dry Ingredients",
                    description = "In a mason jar or glass bowl, combine rolled oats, whey protein isolate, chia seeds, and raw cacao powder. Whisk with a fork to disperse powder.",
                    durationMinutes = 2
                ),
                RecipeStep(
                    stepNumber = 2,
                    title = "Add Liquids & Peanut Butter",
                    description = "Pour in unsweetened almond milk and stir vigorously until completely smooth. Swirl in peanut butter.",
                    durationMinutes = 2,
                    proTip = "Microwave peanut butter for 10 seconds to make it drizzle easily into the mixture."
                ),
                RecipeStep(
                    stepNumber = 3,
                    title = "Refrigerate Overnight",
                    description = "Seal the container and place in the refrigerator for at least 4 hours (or overnight for optimal creamy pudding texture).",
                    durationMinutes = 0
                ),
                RecipeStep(
                    stepNumber = 4,
                    title = "Top and Enjoy",
                    description = "In the morning, open jar, give a gentle stir, and top with fresh banana slices, dark chocolate chips, or a sprinkle of cinnamon.",
                    durationMinutes = 2
                )
            ),
            chefTip = "Chia seeds absorb 10x their weight in liquid, creating a thick pudding texture packed with anti-inflammatory omega-3s."
        ),
        FitnessRecipe(
            id = "rec_beef_quinoa_fiesta",
            title = "Grass-Fed Lean Beef & Quinoa Fiesta Power Bowl",
            category = "Dinner",
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 1,
            difficulty = "Easy",
            calories = 580,
            proteinG = 48,
            carbsG = 52,
            fatG = 18,
            fiberG = 8,
            description = "Seasoned grass-fed ground beef, black beans, sweet corn, sautéed bell peppers, and fresh cilantro lime salsa over cooked quinoa.",
            tags = listOf("Iron Rich", "High Protein", "Strength Fuel", "Gluten-Free"),
            ingredients = listOf(
                RecipeIngredient("Lean Ground Beef 93/7", "200g", "🥩"),
                RecipeIngredient("Cooked Quinoa or Brown Rice", "140g (1 cup)", "🌾"),
                RecipeIngredient("Black Beans (rinsed & drained)", "60g (1/3 cup)", "🫘"),
                RecipeIngredient("Sweet Corn Kernels", "40g (1/4 cup)", "🌽"),
                RecipeIngredient("Red & Yellow Bell Peppers (diced)", "1/2 cup", "🫑"),
                RecipeIngredient("Taco Seasoning (Cumin, Chili, Garlic)", "1 tbsp", "🌶️"),
                RecipeIngredient("Fresh Cilantro & Lime Wedge", "1 tbsp chopped", "🌿"),
                RecipeIngredient("Guacamole / Avocado", "2 tbsp (30g)", "🥑")
            ),
            steps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    title = "Brown the Ground Beef",
                    description = "Heat a skillet over medium-high heat. Add ground beef and break apart with a wooden spoon. Cook for 6-8 minutes until browned.",
                    durationMinutes = 8,
                    proTip = "Drain excess rendered fat if desired to keep macros lean."
                ),
                RecipeStep(
                    stepNumber = 2,
                    title = "Spice and Simmer",
                    description = "Add taco seasoning, 2 tbsp water, rinsed black beans, and sweet corn. Simmer on low heat for 3 minutes until aromatic.",
                    durationMinutes = 3
                ),
                RecipeStep(
                    stepNumber = 3,
                    title = "Sauté Bell Peppers",
                    description = "In a separate pan, blister diced bell peppers with a pinch of salt over high heat for 3 minutes for a smoky crunch.",
                    durationMinutes = 3
                ),
                RecipeStep(
                    stepNumber = 4,
                    title = "Assemble Fiesta Bowl",
                    description = "Place warm quinoa in a bowl, top with seasoned beef mix, sautéed peppers, guacamole, fresh cilantro, and a generous squeeze of fresh lime juice.",
                    durationMinutes = 2
                )
            ),
            chefTip = "Grass-fed beef provides natural dietary creatine, bioavailable heme iron, and zinc to support testosterone and peak athletic output."
        ),
        FitnessRecipe(
            id = "rec_green_power_smoothie",
            title = "Green Monster Anabolic Detox Smoothie",
            category = "Post-Workout",
            prepTimeMinutes = 5,
            cookTimeMinutes = 0,
            servings = 1,
            difficulty = "Easy",
            calories = 310,
            proteinG = 34,
            carbsG = 32,
            fatG = 4,
            fiberG = 6,
            description = "Ultra-refreshing recovery blend of baby spinach, frozen pineapple, crisp green apple, vanilla whey isolate protein, coconut water, and fresh ginger.",
            tags = listOf("Hydration", "Fast Absorption", "Electrolytes", "Post-Workout"),
            ingredients = listOf(
                RecipeIngredient("Vanilla Whey Isolate Protein", "1.2 scoops (35g)", "🥛"),
                RecipeIngredient("Baby Spinach Leaves", "50g (2 packed cups)", "🥬"),
                RecipeIngredient("Frozen Pineapple Chunks", "80g (1/2 cup)", "🍍"),
                RecipeIngredient("Green Apple (cored & sliced)", "1/2 medium", "🍏"),
                RecipeIngredient("Pure Coconut Water", "250ml (1 cup)", "🥥"),
                RecipeIngredient("Fresh Ginger Root (grated)", "1/2 tsp", "🫚"),
                RecipeIngredient("Ice Cubes", "4-5 cubes", "🧊")
            ),
            steps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    title = "Load the Blender",
                    description = "Add coconut water first, followed by spinach, fresh ginger, green apple slices, frozen pineapple chunks, and whey isolate powder.",
                    durationMinutes = 2,
                    proTip = "Pouring liquids at the bottom prevents protein powder from sticking to the blender blade."
                ),
                RecipeStep(
                    stepNumber = 2,
                    title = "Blend to Silky Smoothness",
                    description = "Blend on high speed for 45-60 seconds until vibrant emerald green and completely emulsified.",
                    durationMinutes = 1
                ),
                RecipeStep(
                    stepNumber = 3,
                    title = "Pour and Hydrate",
                    description = "Pour into a tall chilled glass. Drink within 30 minutes post-workout for optimal glycogen and amino acid replenishment.",
                    durationMinutes = 1
                )
            ),
            chefTip = "Pineapple contains bromelain, a natural proteolytic enzyme that reduces muscle soreness and speeds protein breakdown."
        ),
        FitnessRecipe(
            id = "rec_turkey_meatballs_zoodles",
            title = "Tender Garlic Turkey Meatballs with Zucchini Pasta",
            category = "Dinner",
            prepTimeMinutes = 15,
            cookTimeMinutes = 18,
            servings = 1,
            difficulty = "Intermediate",
            calories = 430,
            proteinG = 49,
            carbsG = 18,
            fatG = 16,
            fiberG = 5,
            description = "Lean ground turkey meatballs seasoned with Italian herbs and garlic, baked to juicy perfection and tossed in marinara over spiralized zucchini noodles.",
            tags = listOf("Keto-Friendly", "High Protein", "Low Carb", "Lean Muscle"),
            ingredients = listOf(
                RecipeIngredient("Lean Ground Turkey 93/7", "220g", "🦃"),
                RecipeIngredient("Medium Zucchini (spiralized)", "2 whole (300g)", "🥒"),
                RecipeIngredient("Low-Sugar Marinara Sauce", "120g (1/2 cup)", "🍅"),
                RecipeIngredient("Grated Parmesan Cheese", "15g (2 tbsp)", "🧀"),
                RecipeIngredient("Italian Seasoning & Garlic (minced)", "1 tsp herbs + 2 cloves garlic", "🧄"),
                RecipeIngredient("Egg White", "1 egg white (30ml)", "🥚"),
                RecipeIngredient("Sea Salt & Crushed Black Pepper", "To taste", "🧂")
            ),
            steps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    title = "Mix & Shape Meatballs",
                    description = "In a bowl, combine ground turkey, egg white, minced garlic, Italian herbs, parmesan, salt, and pepper. Form into 6-8 uniform meatballs.",
                    durationMinutes = 5
                ),
                RecipeStep(
                    stepNumber = 2,
                    title = "Bake the Meatballs",
                    description = "Place meatballs on a parchment-lined baking tray. Bake at 190°C (375°F) for 15-18 minutes until golden and thoroughly cooked.",
                    durationMinutes = 18,
                    proTip = "Baking rather than frying preserves moisture without adding extra cooking oils."
                ),
                RecipeStep(
                    stepNumber = 3,
                    title = "Flash-Sauté Zucchini Noodles",
                    description = "Spiralize zucchini into noodles. Sauté in a hot dry skillet with a pinch of salt for only 90 seconds. Drain any moisture.",
                    durationMinutes = 2,
                    proTip = "Do not overcook zoodles, or they will release water. Keep them crisp 'al dente'."
                ),
                RecipeStep(
                    stepNumber = 4,
                    title = "Toss with Warm Marinara",
                    description = "Warm marinara sauce in pan. Add meatballs and spoon over zucchini noodles. Top with grated parmesan and fresh basil.",
                    durationMinutes = 2
                )
            ),
            chefTip = "Ground turkey is low in saturated fat and high in tryptophan, promoting restful sleep when consumed at dinner."
        ),
        FitnessRecipe(
            id = "rec_greek_yogurt_parfait",
            title = "Triple Berry Greek Yogurt Power Parfait",
            category = "Snack",
            prepTimeMinutes = 5,
            cookTimeMinutes = 0,
            servings = 1,
            difficulty = "Easy",
            calories = 310,
            proteinG = 28,
            carbsG = 36,
            fatG = 6,
            fiberG = 6,
            description = "Layered 0% authentic Greek yogurt with organic blueberries, raspberries, toasted crushed almonds, raw honey, and chia seeds.",
            tags = listOf("Probiotic", "Quick Snack", "Gut Health", "Antioxidant Rich"),
            ingredients = listOf(
                RecipeIngredient("0% Plain Greek Yogurt", "220g (1 cup)", "🥣"),
                RecipeIngredient("Fresh Mixed Berries (Blueberries, Raspberries)", "80g (3/4 cup)", "🫐"),
                RecipeIngredient("Raw Honey or Pure Maple Syrup", "1 tsp (7g)", "🍯"),
                RecipeIngredient("Toasted Sliced Almonds", "15g (1 tbsp)", "🥜"),
                RecipeIngredient("Chia Seeds & Ground Flaxseed", "1 tsp each (8g)", "🌱"),
                RecipeIngredient("Ground Cinnamon", "Pinch", "🍂")
            ),
            steps = listOf(
                RecipeStep(
                    stepNumber = 1,
                    title = "Season the Greek Yogurt",
                    description = "In a bowl, mix Greek yogurt with a pinch of ground cinnamon and half the raw honey.",
                    durationMinutes = 2
                ),
                RecipeStep(
                    stepNumber = 2,
                    title = "Layer the Parfait",
                    description = "In a clear glass or bowl, add half the Greek yogurt. Top with a layer of mixed berries and chia seeds. Add remaining yogurt.",
                    durationMinutes = 2
                ),
                RecipeStep(
                    stepNumber = 3,
                    title = "Garnish with Crunch",
                    description = "Top with toasted sliced almonds, remaining fresh berries, and a drizzle of raw honey.",
                    durationMinutes = 1
                )
            ),
            chefTip = "Greek yogurt contains slow-digesting micellar casein protein, providing a sustained release of amino acids for several hours."
        )
    )
}
