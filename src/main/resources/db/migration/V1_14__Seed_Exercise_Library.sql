-- ============================================================================
-- V1_14: Seed Exercise Library with Categories and Common Exercises
-- Description: Populates exercise_categories and exercises tables with
--              public exercise library (70 common exercises)
-- Dependencies: V1_13 (exercise_categories and exercises tables)
-- ============================================================================

-- ============================================================================
-- Insert Exercise Categories
-- ============================================================================

INSERT INTO exercise_categories (name, description, display_order) VALUES
('Strength', 'Resistance training and weightlifting exercises for building muscle mass and strength', 1),
('Cardio', 'Cardiovascular and aerobic exercises for heart health and endurance', 2),
('Flexibility', 'Stretching and mobility exercises for improving range of motion', 3),
('Plyometrics', 'Explosive power and jump training for athletic performance', 4),
('Core', 'Abdominal and core stability exercises for trunk strength', 5),
('Sports', 'Sport-specific training exercises and drills', 6),
('Recovery', 'Cool-down, foam rolling, and recovery exercises', 7);

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

    INSERT INTO exercises (name, description, category_id, primary_muscle_group, secondary_muscle_groups, equipment_required, difficulty_level, instructions, is_public) VALUES
    ('Barbell Bench Press', 'Classic compound chest exercise for building mass and upper body strength', strength_cat_id, 'Chest',
     ARRAY['Triceps', 'Shoulders'], 'Barbell, Bench', 'INTERMEDIATE',
     '["Lie flat on bench with feet planted on floor", "Grip bar slightly wider than shoulder width", "Unrack bar and position over chest", "Lower bar to mid-chest with control", "Press bar back up to starting position", "Keep shoulder blades retracted throughout movement"]'::jsonb, true),

    ('Dumbbell Chest Press', 'Chest exercise with greater range of motion and unilateral stability demands', strength_cat_id, 'Chest',
     ARRAY['Triceps', 'Shoulders'], 'Dumbbells, Bench', 'BEGINNER',
     '["Lie on bench with dumbbells in each hand", "Start with arms extended above chest", "Lower dumbbells until elbows reach 90 degrees", "Press dumbbells back up while squeezing chest", "Keep core engaged throughout"]'::jsonb, true),

    ('Incline Dumbbell Press', 'Upper chest focused variation of chest press', strength_cat_id, 'Chest',
     ARRAY['Shoulders', 'Triceps'], 'Dumbbells, Incline Bench', 'INTERMEDIATE',
     '["Set bench to 30-45 degree incline", "Sit with back firmly against bench", "Start with dumbbells at shoulder level", "Press dumbbells up and slightly together", "Lower with control to starting position"]'::jsonb, true),

    ('Push-ups', 'Bodyweight chest and tricep exercise', strength_cat_id, 'Chest',
     ARRAY['Triceps', 'Shoulders', 'Core'], 'None', 'BEGINNER',
     '["Start in plank position with hands shoulder-width apart", "Keep body in straight line from head to heels", "Lower body until chest nearly touches ground", "Push back up to starting position", "Keep core tight throughout movement"]'::jsonb, true),

    ('Chest Fly', 'Isolation exercise targeting chest muscles', strength_cat_id, 'Chest',
     ARRAY['Shoulders'], 'Dumbbells, Bench', 'INTERMEDIATE',
     '["Lie flat on bench with dumbbells above chest", "Keep slight bend in elbows", "Lower dumbbells out to sides in arc motion", "Bring dumbbells back together above chest", "Focus on chest contraction"]'::jsonb, true),

    ('Dips', 'Compound bodyweight exercise for chest and triceps', strength_cat_id, 'Chest',
     ARRAY['Triceps', 'Shoulders'], 'Dip Bars', 'INTERMEDIATE',
     '["Support body on parallel bars with arms extended", "Lean forward slightly for chest emphasis", "Lower body by bending elbows to 90 degrees", "Push back up to starting position", "Keep shoulders down and back"]'::jsonb, true),

    -- ========================================================================
    -- BACK EXERCISES (Strength)
    -- ========================================================================

    ('Deadlift', 'King of compound exercises engaging entire posterior chain', strength_cat_id, 'Back',
     ARRAY['Hamstrings', 'Glutes', 'Traps', 'Forearms'], 'Barbell', 'ADVANCED',
     '["Stand with barbell over midfoot", "Grip bar just outside legs", "Keep back straight and chest up", "Drive through heels to lift bar", "Stand fully upright at top", "Lower bar with control by hinging hips"]'::jsonb, true),

    ('Pull-ups', 'Upper back and lat bodyweight exercise', strength_cat_id, 'Back',
     ARRAY['Biceps', 'Forearms'], 'Pull-up Bar', 'INTERMEDIATE',
     '["Hang from bar with overhand grip slightly wider than shoulders", "Pull body up until chin clears bar", "Keep core tight and avoid swinging", "Lower body with control to full hang", "Focus on pulling with back muscles"]'::jsonb, true),

    ('Barbell Row', 'Horizontal pulling exercise for back thickness', strength_cat_id, 'Back',
     ARRAY['Biceps', 'Traps', 'Rear Delts'], 'Barbell', 'INTERMEDIATE',
     '["Bend at hips with flat back at 45-degree angle", "Grip bar with hands shoulder-width apart", "Pull bar to lower chest/upper abdomen", "Squeeze shoulder blades together at top", "Lower bar with control"]'::jsonb, true),

    ('Lat Pulldown', 'Machine-based lat development exercise', strength_cat_id, 'Back',
     ARRAY['Biceps', 'Rear Delts'], 'Lat Pulldown Machine', 'BEGINNER',
     '["Sit at machine with thighs secured under pads", "Grip bar slightly wider than shoulder width", "Pull bar down to upper chest", "Squeeze shoulder blades together", "Return to starting position with control"]'::jsonb, true),

    ('Seated Cable Row', 'Horizontal pulling exercise using cable machine', strength_cat_id, 'Back',
     ARRAY['Biceps', 'Traps'], 'Cable Machine', 'BEGINNER',
     '["Sit at cable row machine with feet on platform", "Grip handle with arms extended", "Pull handle to torso while keeping back straight", "Squeeze shoulder blades at end of movement", "Return with control"]'::jsonb, true),

    ('T-Bar Row', 'Thick back builder using landmine or T-bar machine', strength_cat_id, 'Back',
     ARRAY['Traps', 'Biceps'], 'T-Bar or Landmine', 'INTERMEDIATE',
     '["Straddle bar with bent knees and flat back", "Grip handle with both hands", "Pull bar to chest while keeping torso stable", "Lower with control", "Avoid rounding lower back"]'::jsonb, true),

    -- ========================================================================
    -- LEG EXERCISES (Strength)
    -- ========================================================================

    ('Barbell Squat', 'King of leg exercises for overall lower body development', strength_cat_id, 'Legs',
     ARRAY['Glutes', 'Hamstrings', 'Core'], 'Barbell, Squat Rack', 'INTERMEDIATE',
     '["Position bar on upper back across traps", "Stand with feet shoulder-width apart", "Squat down by breaking at hips and knees", "Descend until thighs are parallel to ground", "Drive through heels to stand", "Keep chest up and core braced"]'::jsonb, true),

    ('Romanian Deadlift', 'Hamstring and glute focused hip hinge movement', strength_cat_id, 'Legs',
     ARRAY['Glutes', 'Lower Back', 'Hamstrings'], 'Barbell or Dumbbells', 'INTERMEDIATE',
     '["Hold bar at hip level with slight knee bend", "Hinge at hips while pushing them back", "Lower bar along thighs until feeling hamstring stretch", "Keep back flat throughout movement", "Return to starting position by driving hips forward"]'::jsonb, true),

    ('Leg Press', 'Machine-based compound leg exercise', strength_cat_id, 'Legs',
     ARRAY['Glutes', 'Hamstrings'], 'Leg Press Machine', 'BEGINNER',
     '["Sit in machine with back and head against pad", "Place feet shoulder-width on platform", "Release safety handles", "Lower weight by bending knees", "Press back up without locking knees"]'::jsonb, true),

    ('Walking Lunges', 'Unilateral leg exercise for balance and strength', strength_cat_id, 'Legs',
     ARRAY['Glutes', 'Hamstrings', 'Core'], 'Dumbbells (optional)', 'BEGINNER',
     '["Stand with feet together", "Step forward into lunge position", "Lower back knee toward ground", "Push off front foot to step forward", "Alternate legs with each step", "Keep torso upright"]'::jsonb, true),

    ('Leg Extension', 'Isolation exercise for quadriceps', strength_cat_id, 'Legs',
     ARRAY[], 'Leg Extension Machine', 'BEGINNER',
     '["Sit in machine with back against pad", "Position shins behind pad", "Extend legs to straight position", "Squeeze quadriceps at top", "Lower with control"]'::jsonb, true),

    ('Leg Curl', 'Isolation exercise for hamstrings', strength_cat_id, 'Legs',
     ARRAY['Calves'], 'Leg Curl Machine', 'BEGINNER',
     '["Lie face down on machine", "Position ankles under pad", "Curl legs toward glutes", "Squeeze hamstrings at top", "Lower with control"]'::jsonb, true),

    ('Bulgarian Split Squat', 'Single-leg squat variation for unilateral strength', strength_cat_id, 'Legs',
     ARRAY['Glutes', 'Core'], 'Dumbbells, Bench', 'INTERMEDIATE',
     '["Stand in split stance with rear foot elevated on bench", "Hold dumbbells at sides", "Lower body by bending front knee", "Keep torso upright", "Push through front heel to return"]'::jsonb, true),

    ('Calf Raise', 'Isolation exercise for calf muscles', strength_cat_id, 'Legs',
     ARRAY[], 'Calf Raise Machine or Dumbbells', 'BEGINNER',
     '["Stand with balls of feet on elevated surface", "Rise up onto toes as high as possible", "Squeeze calves at top", "Lower heels below platform level", "Repeat for full range of motion"]'::jsonb, true),

    -- ========================================================================
    -- SHOULDER EXERCISES (Strength)
    -- ========================================================================

    ('Overhead Press', 'Compound shoulder exercise for overall deltoid development', strength_cat_id, 'Shoulders',
     ARRAY['Triceps', 'Upper Chest'], 'Barbell or Dumbbells', 'INTERMEDIATE',
     '["Start with bar at shoulder height", "Grip bar just outside shoulders", "Press weight directly overhead", "Lock out arms at top", "Lower bar with control to shoulders"]'::jsonb, true),

    ('Dumbbell Shoulder Press', 'Seated or standing shoulder press with dumbbells', strength_cat_id, 'Shoulders',
     ARRAY['Triceps'], 'Dumbbells, Bench (optional)', 'BEGINNER',
     '["Hold dumbbells at shoulder height", "Press dumbbells overhead until arms extended", "Lower with control back to shoulders", "Keep core engaged", "Avoid arching back excessively"]'::jsonb, true),

    ('Lateral Raises', 'Isolation exercise for side deltoids', strength_cat_id, 'Shoulders',
     ARRAY[], 'Dumbbells', 'BEGINNER',
     '["Stand with dumbbells at sides", "Keep slight bend in elbows", "Raise arms out to sides to shoulder height", "Pause at top", "Lower slowly to starting position", "Avoid swinging or using momentum"]'::jsonb, true),

    ('Front Raises', 'Isolation exercise for front deltoids', strength_cat_id, 'Shoulders',
     ARRAY[], 'Dumbbells or Barbell', 'BEGINNER',
     '["Stand holding weight in front of thighs", "Raise weight forward to shoulder height", "Keep arms straight with slight elbow bend", "Lower with control", "Avoid swinging or leaning back"]'::jsonb, true),

    ('Face Pulls', 'Rear deltoid and upper back exercise', strength_cat_id, 'Shoulders',
     ARRAY['Upper Back', 'Traps'], 'Cable Machine with Rope', 'BEGINNER',
     '["Set cable at upper chest height", "Grip rope with thumbs toward you", "Pull rope toward face while separating hands", "Squeeze shoulder blades together", "Return with control"]'::jsonb, true),

    ('Arnold Press', 'Dumbbell shoulder press variation with rotation', strength_cat_id, 'Shoulders',
     ARRAY['Triceps'], 'Dumbbells', 'INTERMEDIATE',
     '["Start with dumbbells at shoulder height, palms facing you", "Press dumbbells up while rotating palms forward", "End with palms facing forward at top", "Reverse motion to return to start", "Engages all three deltoid heads"]'::jsonb, true),

    -- ========================================================================
    -- ARM EXERCISES (Strength)
    -- ========================================================================

    ('Barbell Curl', 'Basic bicep mass builder', strength_cat_id, 'Arms',
     ARRAY['Forearms'], 'Barbell', 'BEGINNER',
     '["Stand holding bar with underhand grip at hip level", "Keep elbows close to sides", "Curl bar up toward shoulders", "Squeeze biceps at top", "Lower with control", "Avoid swinging or using momentum"]'::jsonb, true),

    ('Hammer Curl', 'Bicep and forearm exercise with neutral grip', strength_cat_id, 'Arms',
     ARRAY['Forearms', 'Brachialis'], 'Dumbbells', 'BEGINNER',
     '["Hold dumbbells at sides with palms facing each other", "Curl dumbbells up while maintaining neutral grip", "Keep elbows stationary", "Lower with control"]'::jsonb, true),

    ('Tricep Pushdown', 'Cable isolation exercise for triceps', strength_cat_id, 'Arms',
     ARRAY[], 'Cable Machine', 'BEGINNER',
     '["Stand at cable machine with bar at chest height", "Grip bar with overhand grip", "Push bar down by extending elbows", "Keep elbows close to sides", "Return with control"]'::jsonb, true),

    ('Skull Crushers', 'Lying tricep extension exercise', strength_cat_id, 'Arms',
     ARRAY[], 'EZ Bar or Dumbbells, Bench', 'INTERMEDIATE',
     '["Lie on bench holding bar above chest", "Lower bar toward forehead by bending elbows", "Keep upper arms stationary", "Extend arms back to starting position", "Control the weight carefully"]'::jsonb, true),

    ('Preacher Curl', 'Isolated bicep exercise using preacher bench', strength_cat_id, 'Arms',
     ARRAY[], 'EZ Bar or Dumbbells, Preacher Bench', 'BEGINNER',
     '["Sit at preacher bench with arms over pad", "Curl weight up while keeping upper arms on pad", "Squeeze biceps at top", "Lower with full control", "Prevents swinging"]'::jsonb, true),

    ('Close-Grip Bench Press', 'Compound exercise emphasizing triceps', strength_cat_id, 'Arms',
     ARRAY['Chest', 'Shoulders'], 'Barbell, Bench', 'INTERMEDIATE',
     '["Lie on bench with grip narrower than shoulder width", "Lower bar to lower chest", "Keep elbows close to body", "Press back up focusing on triceps", "Avoid flaring elbows out"]'::jsonb, true),

    -- ========================================================================
    -- CARDIO EXERCISES
    -- ========================================================================

    ('Running', 'Classic cardiovascular exercise', cardio_cat_id, 'Full Body',
     ARRAY['Legs', 'Core'], 'Treadmill or Outdoors', 'BEGINNER',
     '["Start at comfortable warm-up pace", "Gradually increase to target pace", "Maintain steady rhythm and breathing", "Keep posture upright", "Land midfoot with each stride", "Cool down with slower pace"]'::jsonb, true),

    ('Cycling', 'Low-impact cardio exercise', cardio_cat_id, 'Legs',
     ARRAY['Glutes', 'Hamstrings'], 'Bike or Stationary Bike', 'BEGINNER',
     '["Adjust seat height so leg is slightly bent at bottom", "Start with easy resistance", "Pedal at steady cadence (70-90 RPM)", "Vary resistance for interval training", "Maintain upright posture"]'::jsonb, true),

    ('Rowing Machine', 'Full-body cardio with strength component', cardio_cat_id, 'Full Body',
     ARRAY['Back', 'Legs', 'Core'], 'Rowing Machine', 'INTERMEDIATE',
     '["Sit with feet strapped in and knees bent", "Start by pushing with legs", "Lean back slightly and pull handle to chest", "Reverse motion: arms, torso, then legs", "Maintain smooth rhythm", "60% legs, 20% core, 20% arms"]'::jsonb, true),

    ('Jump Rope', 'High-intensity cardio and coordination exercise', cardio_cat_id, 'Full Body',
     ARRAY['Calves', 'Shoulders'], 'Jump Rope', 'INTERMEDIATE',
     '["Hold rope handles at hip height", "Jump with both feet together", "Rotate rope with wrists, not arms", "Land softly on balls of feet", "Start with short intervals", "Build up duration gradually"]'::jsonb, true),

    ('Elliptical', 'Low-impact full-body cardio machine', cardio_cat_id, 'Full Body',
     ARRAY['Legs', 'Arms'], 'Elliptical Machine', 'BEGINNER',
     '["Step onto pedals and grip handles", "Move feet in elliptical motion", "Push and pull handles for arm engagement", "Keep back straight and core engaged", "Adjust resistance as needed"]'::jsonb, true),

    ('Stair Climber', 'Lower body focused cardio machine', cardio_cat_id, 'Legs',
     ARRAY['Glutes', 'Calves'], 'Stair Climber Machine', 'BEGINNER',
     '["Step onto machine and grip handrails lightly", "Climb stairs at steady pace", "Keep upright posture", "Avoid leaning heavily on rails", "Engage glutes with each step"]'::jsonb, true),

    ('Swimming', 'Full-body low-impact cardio', cardio_cat_id, 'Full Body',
     ARRAY['Back', 'Shoulders', 'Core'], 'Pool', 'INTERMEDIATE',
     '["Choose stroke (freestyle, breaststroke, etc.)", "Focus on proper breathing technique", "Use full range of motion", "Maintain streamlined body position", "Vary strokes for complete workout"]'::jsonb, true),

    ('Burpees', 'High-intensity bodyweight cardio exercise', cardio_cat_id, 'Full Body',
     ARRAY['Chest', 'Legs', 'Core'], 'None', 'ADVANCED',
     '["Start standing", "Drop to squat position with hands on ground", "Kick feet back to push-up position", "Perform push-up", "Jump feet back to squat", "Jump up explosively with arms overhead", "Repeat continuously"]'::jsonb, true),

    -- ========================================================================
    -- CORE EXERCISES
    -- ========================================================================

    ('Plank', 'Isometric core stability exercise', core_cat_id, 'Core',
     ARRAY['Shoulders', 'Back'], 'None', 'BEGINNER',
     '["Start in push-up position", "Lower to forearms", "Keep body in straight line from head to heels", "Engage core and glutes", "Hold position without sagging hips", "Breathe steadily"]'::jsonb, true),

    ('Russian Twists', 'Rotational core exercise', core_cat_id, 'Core',
     ARRAY['Obliques'], 'Medicine Ball (optional)', 'BEGINNER',
     '["Sit on ground with knees bent", "Lean back slightly while keeping back straight", "Rotate torso side to side", "Touch ground beside hip with each rotation", "Keep core engaged throughout"]'::jsonb, true),

    ('Bicycle Crunches', 'Dynamic ab exercise targeting obliques', core_cat_id, 'Core',
     ARRAY['Obliques'], 'None', 'BEGINNER',
     '["Lie on back with hands behind head", "Bring opposite elbow to opposite knee", "Extend other leg straight", "Alternate sides in pedaling motion", "Keep lower back pressed to ground", "Focus on rotation"]'::jsonb, true),

    ('Hanging Leg Raises', 'Advanced lower ab exercise', core_cat_id, 'Core',
     ARRAY['Hip Flexors'], 'Pull-up Bar', 'ADVANCED',
     '["Hang from pull-up bar with straight arms", "Keep legs together", "Raise legs to 90 degrees or higher", "Lower with control", "Avoid swinging", "Can bend knees for easier variation"]'::jsonb, true),

    ('Ab Wheel Rollout', 'Advanced core stability exercise', core_cat_id, 'Core',
     ARRAY['Shoulders', 'Back'], 'Ab Wheel', 'ADVANCED',
     '["Kneel on ground holding ab wheel", "Roll wheel forward while keeping core tight", "Extend as far as possible without sagging", "Pull back to starting position using core", "Keep back flat throughout"]'::jsonb, true),

    ('Mountain Climbers', 'Dynamic core and cardio exercise', core_cat_id, 'Core',
     ARRAY['Shoulders', 'Legs'], 'None', 'INTERMEDIATE',
     '["Start in push-up position", "Drive one knee toward chest", "Quickly switch legs", "Alternate legs rapidly", "Keep hips level", "Maintain plank position throughout"]'::jsonb, true),

    ('Side Plank', 'Oblique-focused isometric hold', core_cat_id, 'Core',
     ARRAY['Obliques', 'Shoulders'], 'None', 'INTERMEDIATE',
     '["Lie on side with forearm on ground", "Stack feet or stagger for easier variation", "Lift hips off ground", "Keep body in straight line", "Hold position", "Repeat on other side"]'::jsonb, true),

    ('Dead Bug', 'Core stability exercise with coordination', core_cat_id, 'Core',
     ARRAY[], 'None', 'BEGINNER',
     '["Lie on back with arms extended toward ceiling", "Lift legs with knees at 90 degrees", "Lower opposite arm and leg toward ground", "Return to center and switch sides", "Keep lower back pressed to ground", "Move slowly with control"]'::jsonb, true),

    -- ========================================================================
    -- PLYOMETRIC EXERCISES
    -- ========================================================================

    ('Box Jumps', 'Explosive lower body power exercise', plyo_cat_id, 'Legs',
     ARRAY['Glutes', 'Calves'], 'Plyometric Box', 'INTERMEDIATE',
     '["Stand facing box at appropriate height", "Swing arms and jump explosively onto box", "Land softly with knees bent", "Stand fully upright on box", "Step down carefully", "Reset and repeat"]'::jsonb, true),

    ('Jump Squats', 'Explosive squat variation', plyo_cat_id, 'Legs',
     ARRAY['Glutes', 'Calves'], 'None', 'INTERMEDIATE',
     '["Start in squat position", "Explode upward jumping as high as possible", "Swing arms for momentum", "Land softly and immediately descend into next rep", "Keep core engaged"]'::jsonb, true),

    ('Clap Push-ups', 'Explosive upper body plyometric exercise', plyo_cat_id, 'Chest',
     ARRAY['Triceps', 'Shoulders'], 'None', 'ADVANCED',
     '["Start in push-up position", "Lower body to ground", "Push up explosively", "Clap hands together while airborne", "Land with hands in push-up position", "Absorb impact with slight elbow bend"]'::jsonb, true),

    ('Tuck Jumps', 'High-intensity vertical jump exercise', plyo_cat_id, 'Legs',
     ARRAY['Core', 'Hip Flexors'], 'None', 'ADVANCED',
     '["Stand with feet shoulder-width apart", "Jump vertically as high as possible", "Bring knees to chest at peak of jump", "Land softly with bent knees", "Immediately repeat"]'::jsonb, true),

    ('Broad Jumps', 'Horizontal power development exercise', plyo_cat_id, 'Legs',
     ARRAY['Glutes', 'Hamstrings'], 'None', 'INTERMEDIATE',
     '["Stand with feet hip-width apart", "Swing arms back while bending knees", "Explosively jump forward as far as possible", "Land softly on both feet", "Walk back to start"]'::jsonb, true),

    -- ========================================================================
    -- FLEXIBILITY EXERCISES
    -- ========================================================================

    ('Standing Hamstring Stretch', 'Basic hamstring flexibility stretch', flex_cat_id, 'Hamstrings',
     ARRAY[], 'None', 'BEGINNER',
     '["Stand with one foot elevated on platform", "Keep leg straight", "Hinge forward at hips until feeling stretch", "Hold for 20-30 seconds", "Switch legs"]'::jsonb, true),

    ('Quad Stretch', 'Standing quadriceps stretch', flex_cat_id, 'Quadriceps',
     ARRAY[], 'None', 'BEGINNER',
     '["Stand on one leg", "Grab opposite ankle behind you", "Pull heel toward glutes", "Keep knees together", "Hold for 20-30 seconds", "Switch legs"]'::jsonb, true),

    ('Child''s Pose', 'Resting yoga pose for back and shoulders', flex_cat_id, 'Back',
     ARRAY['Shoulders', 'Hips'], 'Yoga Mat', 'BEGINNER',
     '["Kneel on mat with big toes touching", "Sit back on heels", "Extend arms forward on mat", "Lower forehead to ground", "Hold while breathing deeply", "Feel stretch through back and shoulders"]'::jsonb, true),

    ('Cat-Cow Stretch', 'Spinal mobility exercise', flex_cat_id, 'Back',
     ARRAY['Core'], 'Yoga Mat', 'BEGINNER',
     '["Start on hands and knees", "Arch back while lifting head (cow)", "Round back while tucking chin (cat)", "Alternate between positions slowly", "Coordinate with breathing", "Repeat 10-15 times"]'::jsonb, true),

    ('Pigeon Pose', 'Hip flexibility yoga pose', flex_cat_id, 'Hips',
     ARRAY['Glutes'], 'Yoga Mat', 'INTERMEDIATE',
     '["Start in downward dog or plank", "Bring one knee forward between hands", "Extend back leg straight behind", "Square hips to front", "Hold stretch for 30-60 seconds", "Switch sides"]'::jsonb, true),

    -- ========================================================================
    -- RECOVERY EXERCISES
    -- ========================================================================

    ('Foam Rolling - IT Band', 'Self-myofascial release for IT band', recovery_cat_id, 'Legs',
     ARRAY['Hips'], 'Foam Roller', 'BEGINNER',
     '["Lie on side with foam roller under thigh", "Support upper body with forearm", "Roll from hip to just above knee", "Pause on tender spots", "Apply controlled pressure", "Repeat other leg"]'::jsonb, true),

    ('Foam Rolling - Upper Back', 'Thoracic spine mobility and release', recovery_cat_id, 'Back',
     ARRAY[], 'Foam Roller', 'BEGINNER',
     '["Lie with foam roller across upper back", "Support head with hands", "Lift hips off ground", "Roll up and down thoracic spine", "Pause on tight areas", "Avoid lower back"]'::jsonb, true),

    ('Walking', 'Active recovery and light cardio', recovery_cat_id, 'Full Body',
     ARRAY[], 'None', 'BEGINNER',
     '["Walk at comfortable pace", "Maintain good posture", "Swing arms naturally", "Breathe rhythmically", "Duration: 15-30 minutes", "Perfect for recovery days"]'::jsonb, true);

END $$;

-- ============================================================================
-- Verification Query (Optional - Comment out for production)
-- ============================================================================

-- SELECT
--     ec.name as category,
--     COUNT(e.id) as exercise_count
-- FROM exercise_categories ec
-- LEFT JOIN exercises e ON e.category_id = ec.id
-- GROUP BY ec.name, ec.display_order
-- ORDER BY ec.display_order;
