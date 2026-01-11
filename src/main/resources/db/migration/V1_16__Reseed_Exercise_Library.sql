-- ============================================================================
-- V1_16: Re-seed Exercise Library
-- ============================================================================
-- This migration re-populates exercise_categories and exercises tables
-- after V1_15 recreated them with the correct schema.
--
-- Note: This is essentially a copy of V1_14 since the tables were recreated.
-- ============================================================================

-- ============================================================================
-- Insert Exercise Categories
-- ============================================================================

INSERT INTO exercise_categories (name, description, display_order, is_active) VALUES
('Strength', 'Resistance training and weightlifting exercises for building muscle mass and strength', 1, true),
('Cardio', 'Cardiovascular and aerobic exercises for heart health and endurance', 2, true),
('Flexibility', 'Stretching and mobility exercises for improving range of motion', 3, true),
('Plyometrics', 'Explosive power and jump training for athletic performance', 4, true),
('Core', 'Abdominal and core stability exercises for trunk strength', 5, true),
('Sports', 'Sport-specific training exercises and drills', 6, true),
('Recovery', 'Cool-down, foam rolling, and recovery exercises', 7, true)
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- Insert Common Exercises
-- ============================================================================

DO $$
DECLARE
    strength_cat_id UUID;
    cardio_cat_id UUID;
    flex_cat_id UUID;
    plyo_cat_id UUID;
    core_cat_id UUID;
    sports_cat_id UUID;
    recovery_cat_id UUID;
BEGIN
    -- Get category IDs
    SELECT id INTO strength_cat_id FROM exercise_categories WHERE name = 'Strength';
    SELECT id INTO cardio_cat_id FROM exercise_categories WHERE name = 'Cardio';
    SELECT id INTO flex_cat_id FROM exercise_categories WHERE name = 'Flexibility';
    SELECT id INTO plyo_cat_id FROM exercise_categories WHERE name = 'Plyometrics';
    SELECT id INTO core_cat_id FROM exercise_categories WHERE name = 'Core';
    SELECT id INTO sports_cat_id FROM exercise_categories WHERE name = 'Sports';
    SELECT id INTO recovery_cat_id FROM exercise_categories WHERE name = 'Recovery';

    -- ========================================================================
    -- CHEST EXERCISES (Strength)
    -- ========================================================================

    INSERT INTO exercises (name, description, category_id, primary_muscle_group, secondary_muscle_groups, equipment_required, difficulty_level, instructions, is_public, is_active) VALUES
    ('Barbell Bench Press', 'Classic compound chest exercise for building mass and upper body strength', strength_cat_id, 'Chest',
     ARRAY['Triceps', 'Shoulders'], 'Barbell, Bench', 'INTERMEDIATE',
     '["Lie flat on bench with feet planted on floor", "Grip bar slightly wider than shoulder width", "Unrack bar and position over chest", "Lower bar to mid-chest with control", "Press bar back up to starting position"]'::jsonb, true, true),

    ('Dumbbell Chest Press', 'Chest exercise with greater range of motion', strength_cat_id, 'Chest',
     ARRAY['Triceps', 'Shoulders'], 'Dumbbells, Bench', 'BEGINNER',
     '["Lie on bench with dumbbells in each hand", "Start with arms extended above chest", "Lower dumbbells until elbows reach 90 degrees", "Press dumbbells back up while squeezing chest"]'::jsonb, true, true),

    ('Incline Dumbbell Press', 'Upper chest focused variation', strength_cat_id, 'Chest',
     ARRAY['Shoulders', 'Triceps'], 'Dumbbells, Incline Bench', 'INTERMEDIATE',
     '["Set bench to 30-45 degree incline", "Press dumbbells up and slightly together", "Lower with control to starting position"]'::jsonb, true, true),

    ('Push-ups', 'Bodyweight chest and tricep exercise', strength_cat_id, 'Chest',
     ARRAY['Triceps', 'Shoulders', 'Core'], 'None', 'BEGINNER',
     '["Start in plank position with hands shoulder-width apart", "Keep body in straight line", "Lower body until chest nearly touches ground", "Push back up"]'::jsonb, true, true),

    ('Chest Fly', 'Isolation exercise targeting chest', strength_cat_id, 'Chest',
     ARRAY['Shoulders'], 'Dumbbells, Bench', 'INTERMEDIATE',
     '["Lie flat on bench with dumbbells above chest", "Lower dumbbells out to sides in arc motion", "Bring dumbbells back together"]'::jsonb, true, true),

    ('Dips', 'Compound bodyweight exercise for chest and triceps', strength_cat_id, 'Chest',
     ARRAY['Triceps', 'Shoulders'], 'Dip Bars', 'INTERMEDIATE',
     '["Support body on parallel bars", "Lean forward slightly for chest emphasis", "Lower body by bending elbows to 90 degrees", "Push back up"]'::jsonb, true, true),

    -- ========================================================================
    -- BACK EXERCISES (Strength)
    -- ========================================================================

    ('Deadlift', 'King of compound exercises for posterior chain', strength_cat_id, 'Back',
     ARRAY['Hamstrings', 'Glutes', 'Traps', 'Forearms'], 'Barbell', 'ADVANCED',
     '["Stand with barbell over midfoot", "Grip bar just outside legs", "Keep back straight and chest up", "Drive through heels to lift bar", "Stand fully upright at top"]'::jsonb, true, true),

    ('Pull-ups', 'Upper back and lat bodyweight exercise', strength_cat_id, 'Back',
     ARRAY['Biceps', 'Forearms'], 'Pull-up Bar', 'INTERMEDIATE',
     '["Hang from bar with overhand grip", "Pull body up until chin clears bar", "Lower body with control"]'::jsonb, true, true),

    ('Barbell Row', 'Horizontal pulling exercise for back thickness', strength_cat_id, 'Back',
     ARRAY['Biceps', 'Traps', 'Rear Delts'], 'Barbell', 'INTERMEDIATE',
     '["Bend at hips with flat back", "Pull bar to lower chest", "Squeeze shoulder blades together at top"]'::jsonb, true, true),

    ('Lat Pulldown', 'Machine-based lat development', strength_cat_id, 'Back',
     ARRAY['Biceps', 'Rear Delts'], 'Lat Pulldown Machine', 'BEGINNER',
     '["Sit at machine with thighs secured", "Pull bar down to upper chest", "Return with control"]'::jsonb, true, true),

    ('Seated Cable Row', 'Horizontal pulling using cable machine', strength_cat_id, 'Back',
     ARRAY['Biceps', 'Traps'], 'Cable Machine', 'BEGINNER',
     '["Sit at cable row machine", "Pull handle to torso while keeping back straight", "Squeeze shoulder blades at end"]'::jsonb, true, true),

    -- ========================================================================
    -- LEG EXERCISES (Strength)
    -- ========================================================================

    ('Barbell Squat', 'King of leg exercises', strength_cat_id, 'Legs',
     ARRAY['Glutes', 'Hamstrings', 'Core'], 'Barbell, Squat Rack', 'INTERMEDIATE',
     '["Position bar on upper back", "Squat down until thighs are parallel", "Drive through heels to stand"]'::jsonb, true, true),

    ('Romanian Deadlift', 'Hamstring and glute focused hip hinge', strength_cat_id, 'Legs',
     ARRAY['Glutes', 'Lower Back', 'Hamstrings'], 'Barbell or Dumbbells', 'INTERMEDIATE',
     '["Hold bar at hip level", "Hinge at hips while pushing them back", "Lower bar along thighs until feeling hamstring stretch"]'::jsonb, true, true),

    ('Leg Press', 'Machine-based compound leg exercise', strength_cat_id, 'Legs',
     ARRAY['Glutes', 'Hamstrings'], 'Leg Press Machine', 'BEGINNER',
     '["Sit in machine with back against pad", "Lower weight by bending knees", "Press back up without locking knees"]'::jsonb, true, true),

    ('Walking Lunges', 'Unilateral leg exercise', strength_cat_id, 'Legs',
     ARRAY['Glutes', 'Hamstrings', 'Core'], 'Dumbbells (optional)', 'BEGINNER',
     '["Step forward into lunge position", "Lower back knee toward ground", "Push off front foot to step forward"]'::jsonb, true, true),

    ('Bulgarian Split Squat', 'Single-leg squat variation', strength_cat_id, 'Legs',
     ARRAY['Glutes', 'Core'], 'Dumbbells, Bench', 'INTERMEDIATE',
     '["Stand with rear foot elevated on bench", "Lower body by bending front knee", "Push through front heel to return"]'::jsonb, true, true),

    ('Calf Raise', 'Isolation exercise for calves', strength_cat_id, 'Legs',
     ARRAY[], 'Calf Raise Machine or Dumbbells', 'BEGINNER',
     '["Stand with balls of feet on elevated surface", "Rise up onto toes", "Lower heels below platform level"]'::jsonb, true, true),

    -- ========================================================================
    -- SHOULDER EXERCISES (Strength)
    -- ========================================================================

    ('Overhead Press', 'Compound shoulder exercise', strength_cat_id, 'Shoulders',
     ARRAY['Triceps', 'Upper Chest'], 'Barbell or Dumbbells', 'INTERMEDIATE',
     '["Start with bar at shoulder height", "Press weight directly overhead", "Lower with control"]'::jsonb, true, true),

    ('Lateral Raises', 'Isolation for side deltoids', strength_cat_id, 'Shoulders',
     ARRAY[], 'Dumbbells', 'BEGINNER',
     '["Stand with dumbbells at sides", "Raise arms out to sides to shoulder height", "Lower slowly"]'::jsonb, true, true),

    ('Face Pulls', 'Rear deltoid and upper back exercise', strength_cat_id, 'Shoulders',
     ARRAY['Upper Back', 'Traps'], 'Cable Machine with Rope', 'BEGINNER',
     '["Pull rope toward face while separating hands", "Squeeze shoulder blades together"]'::jsonb, true, true),

    -- ========================================================================
    -- ARM EXERCISES (Strength)
    -- ========================================================================

    ('Barbell Curl', 'Basic bicep mass builder', strength_cat_id, 'Arms',
     ARRAY['Forearms'], 'Barbell', 'BEGINNER',
     '["Stand holding bar with underhand grip", "Curl bar up toward shoulders", "Lower with control"]'::jsonb, true, true),

    ('Hammer Curl', 'Bicep and forearm exercise with neutral grip', strength_cat_id, 'Arms',
     ARRAY['Forearms', 'Brachialis'], 'Dumbbells', 'BEGINNER',
     '["Hold dumbbells with palms facing each other", "Curl dumbbells up while maintaining neutral grip"]'::jsonb, true, true),

    ('Tricep Pushdown', 'Cable isolation for triceps', strength_cat_id, 'Arms',
     ARRAY[], 'Cable Machine', 'BEGINNER',
     '["Stand at cable machine", "Push bar down by extending elbows", "Keep elbows close to sides"]'::jsonb, true, true),

    ('Skull Crushers', 'Lying tricep extension', strength_cat_id, 'Arms',
     ARRAY[], 'EZ Bar or Dumbbells, Bench', 'INTERMEDIATE',
     '["Lie on bench holding bar above chest", "Lower bar toward forehead by bending elbows", "Extend arms back up"]'::jsonb, true, true),

    -- ========================================================================
    -- CARDIO EXERCISES
    -- ========================================================================

    ('Running', 'Classic cardiovascular exercise', cardio_cat_id, 'Full Body',
     ARRAY['Legs', 'Core'], 'Treadmill or Outdoors', 'BEGINNER',
     '["Start at comfortable warm-up pace", "Gradually increase to target pace", "Cool down with slower pace"]'::jsonb, true, true),

    ('Cycling', 'Low-impact cardio exercise', cardio_cat_id, 'Legs',
     ARRAY['Glutes', 'Hamstrings'], 'Bike or Stationary Bike', 'BEGINNER',
     '["Pedal at steady cadence (70-90 RPM)", "Vary resistance for interval training"]'::jsonb, true, true),

    ('Rowing Machine', 'Full-body cardio with strength component', cardio_cat_id, 'Full Body',
     ARRAY['Back', 'Legs', 'Core'], 'Rowing Machine', 'INTERMEDIATE',
     '["Push with legs first", "Lean back slightly and pull handle to chest", "60% legs, 20% core, 20% arms"]'::jsonb, true, true),

    ('Jump Rope', 'High-intensity cardio and coordination', cardio_cat_id, 'Full Body',
     ARRAY['Calves', 'Shoulders'], 'Jump Rope', 'INTERMEDIATE',
     '["Jump with both feet together", "Rotate rope with wrists, not arms", "Land softly"]'::jsonb, true, true),

    ('Burpees', 'High-intensity bodyweight cardio', cardio_cat_id, 'Full Body',
     ARRAY['Chest', 'Legs', 'Core'], 'None', 'ADVANCED',
     '["Drop to squat with hands on ground", "Kick feet back to push-up position", "Jump up explosively with arms overhead"]'::jsonb, true, true),

    -- ========================================================================
    -- CORE EXERCISES
    -- ========================================================================

    ('Plank', 'Isometric core stability exercise', core_cat_id, 'Core',
     ARRAY['Shoulders', 'Back'], 'None', 'BEGINNER',
     '["Start in push-up position on forearms", "Keep body in straight line", "Hold position without sagging hips"]'::jsonb, true, true),

    ('Russian Twists', 'Rotational core exercise', core_cat_id, 'Core',
     ARRAY['Obliques'], 'Medicine Ball (optional)', 'BEGINNER',
     '["Sit on ground with knees bent", "Lean back slightly", "Rotate torso side to side"]'::jsonb, true, true),

    ('Bicycle Crunches', 'Dynamic ab exercise targeting obliques', core_cat_id, 'Core',
     ARRAY['Obliques'], 'None', 'BEGINNER',
     '["Lie on back with hands behind head", "Bring opposite elbow to opposite knee", "Alternate sides"]'::jsonb, true, true),

    ('Hanging Leg Raises', 'Advanced lower ab exercise', core_cat_id, 'Core',
     ARRAY['Hip Flexors'], 'Pull-up Bar', 'ADVANCED',
     '["Hang from pull-up bar", "Raise legs to 90 degrees or higher", "Lower with control"]'::jsonb, true, true),

    ('Mountain Climbers', 'Dynamic core and cardio exercise', core_cat_id, 'Core',
     ARRAY['Shoulders', 'Legs'], 'None', 'INTERMEDIATE',
     '["Start in push-up position", "Drive one knee toward chest", "Quickly alternate legs"]'::jsonb, true, true),

    ('Side Plank', 'Oblique-focused isometric hold', core_cat_id, 'Core',
     ARRAY['Obliques', 'Shoulders'], 'None', 'INTERMEDIATE',
     '["Lie on side with forearm on ground", "Lift hips off ground", "Hold position"]'::jsonb, true, true),

    -- ========================================================================
    -- PLYOMETRIC EXERCISES
    -- ========================================================================

    ('Box Jumps', 'Explosive lower body power exercise', plyo_cat_id, 'Legs',
     ARRAY['Glutes', 'Calves'], 'Plyometric Box', 'INTERMEDIATE',
     '["Stand facing box", "Jump explosively onto box", "Land softly with knees bent"]'::jsonb, true, true),

    ('Jump Squats', 'Explosive squat variation', plyo_cat_id, 'Legs',
     ARRAY['Glutes', 'Calves'], 'None', 'INTERMEDIATE',
     '["Start in squat position", "Explode upward jumping as high as possible", "Land softly"]'::jsonb, true, true),

    ('Broad Jumps', 'Horizontal power development', plyo_cat_id, 'Legs',
     ARRAY['Glutes', 'Hamstrings'], 'None', 'INTERMEDIATE',
     '["Stand with feet hip-width apart", "Jump forward as far as possible", "Land softly"]'::jsonb, true, true),

    -- ========================================================================
    -- FLEXIBILITY EXERCISES
    -- ========================================================================

    ('Standing Hamstring Stretch', 'Basic hamstring flexibility', flex_cat_id, 'Hamstrings',
     ARRAY[], 'None', 'BEGINNER',
     '["Stand with one foot elevated", "Hinge forward at hips", "Hold for 20-30 seconds"]'::jsonb, true, true),

    ('Quad Stretch', 'Standing quadriceps stretch', flex_cat_id, 'Quadriceps',
     ARRAY[], 'None', 'BEGINNER',
     '["Stand on one leg", "Grab opposite ankle behind you", "Pull heel toward glutes"]'::jsonb, true, true),

    ('Cat-Cow Stretch', 'Spinal mobility exercise', flex_cat_id, 'Back',
     ARRAY['Core'], 'Yoga Mat', 'BEGINNER',
     '["Start on hands and knees", "Arch back while lifting head (cow)", "Round back while tucking chin (cat)"]'::jsonb, true, true),

    ('Pigeon Pose', 'Hip flexibility yoga pose', flex_cat_id, 'Hips',
     ARRAY['Glutes'], 'Yoga Mat', 'INTERMEDIATE',
     '["Bring one knee forward between hands", "Extend back leg straight behind", "Hold 30-60 seconds"]'::jsonb, true, true),

    -- ========================================================================
    -- RECOVERY EXERCISES
    -- ========================================================================

    ('Foam Rolling - IT Band', 'Self-myofascial release', recovery_cat_id, 'Legs',
     ARRAY['Hips'], 'Foam Roller', 'BEGINNER',
     '["Lie on side with foam roller under thigh", "Roll from hip to just above knee", "Pause on tender spots"]'::jsonb, true, true),

    ('Foam Rolling - Upper Back', 'Thoracic spine mobility', recovery_cat_id, 'Back',
     ARRAY[], 'Foam Roller', 'BEGINNER',
     '["Lie with foam roller across upper back", "Roll up and down thoracic spine", "Pause on tight areas"]'::jsonb, true, true),

    ('Walking', 'Active recovery', recovery_cat_id, 'Full Body',
     ARRAY[], 'None', 'BEGINNER',
     '["Walk at comfortable pace", "Duration: 15-30 minutes", "Perfect for recovery days"]'::jsonb, true, true);

    RAISE NOTICE 'Exercise library seeding complete!';
END $$;

-- ============================================================================
-- Verification
-- ============================================================================

DO $$
DECLARE
    cat_count INTEGER;
    ex_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO cat_count FROM exercise_categories;
    SELECT COUNT(*) INTO ex_count FROM exercises;

    RAISE NOTICE '============================================';
    RAISE NOTICE 'V1_16 Exercise Library Seeding Complete';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Categories: %', cat_count;
    RAISE NOTICE 'Exercises: %', ex_count;
    RAISE NOTICE '============================================';
END $$;

