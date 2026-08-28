package com.example.engine

import com.example.data.model.GameCategory
import kotlin.random.Random

object InfiniteLevelGenerator {

    fun generateLevel(category: GameCategory, levelNumber: Int): ActiveGameLevel {
        val questionsPerLevel = when {
            levelNumber <= 5 -> 5
            levelNumber <= 20 -> 6
            else -> 7
        }

        val difficulty = when {
            levelNumber <= 5 -> "Novice"
            levelNumber <= 15 -> "Adept"
            levelNumber <= 30 -> "Master"
            else -> "Grandmaster (Lvl $levelNumber)"
        }

        val baseCoin = 25 + (levelNumber * 5)

        val questions = (1..questionsPerLevel).map { qIndex ->
            when (category) {
                GameCategory.SPEED_MATH -> generateMathQuestion(levelNumber, qIndex)
                GameCategory.PATTERN_MATRIX -> generateMatrixQuestion(levelNumber, qIndex)
                GameCategory.STROOP_FOCUS -> generateStroopQuestion(levelNumber, qIndex)
                GameCategory.LOGIC_RIDDLES -> generateLogicQuestion(levelNumber, qIndex)
                GameCategory.WORD_SPARKS -> generateWordQuestion(levelNumber, qIndex)
            }
        }

        return ActiveGameLevel(
            levelNumber = levelNumber,
            gameCategoryTitle = category.title,
            questions = questions,
            targetAccuracyForClear = 0.6f,
            baseCoinReward = baseCoin,
            difficultyLabel = difficulty
        )
    }

    // --- 1. SPEED MATH GENERATOR ---
    private fun generateMathQuestion(level: Int, qIndex: Int): GameQuestion {
        val rand = Random(System.nanoTime() + level * 1000 + qIndex)
        val mode = rand.nextInt(4)

        return when {
            level <= 3 -> {
                // Simple addition & subtraction
                val a = rand.nextInt(5, 20 + level * 5)
                val b = rand.nextInt(3, 15 + level * 3)
                val isAdd = rand.nextBoolean()
                val ans = if (isAdd) a + b else a - b
                val prompt = if (isAdd) "$a + $b = ?" else "$a - $b = ?"
                val options = generateDistractors(ans, 4, rand)
                val correctIndex = options.indexOf(ans.toString())
                GameQuestion(
                    id = "math_${level}_$qIndex",
                    prompt = prompt,
                    subPrompt = "Calculate quickly!",
                    options = options,
                    correctIndex = correctIndex,
                    explanation = "$prompt is $ans",
                    timeLimitSeconds = maxOf(6, 12 - (level / 10)),
                    visualType = VisualType.MATH_OPERATORS
                )
            }
            level <= 10 -> {
                if (mode == 0) {
                    // Missing operator
                    val a = rand.nextInt(3, 12)
                    val b = rand.nextInt(2, 10)
                    val op = listOf("+", "-", "×").random(rand)
                    val ans = when (op) {
                        "+" -> a + b
                        "-" -> a - b
                        else -> a * b
                    }
                    val prompt = "$a  ?  $b = $ans"
                    val options = listOf("+", "-", "×", "÷")
                    GameQuestion(
                        id = "math_${level}_$qIndex",
                        prompt = prompt,
                        subPrompt = "Which operator belongs in the '?' box?",
                        options = options,
                        correctIndex = options.indexOf(op),
                        explanation = "$a $op $b = $ans",
                        timeLimitSeconds = 9,
                        visualType = VisualType.MATH_OPERATORS
                    )
                } else {
                    // Multiplication or Division
                    val a = rand.nextInt(3, 12 + level)
                    val b = rand.nextInt(3, 10 + (level / 2))
                    val ans = a * b
                    val isDiv = rand.nextBoolean()
                    val prompt = if (isDiv) "$ans ÷ $a = ?" else "$a × $b = ?"
                    val correct = if (isDiv) b else ans
                    val options = generateDistractors(correct, 4, rand)
                    GameQuestion(
                        id = "math_${level}_$qIndex",
                        prompt = prompt,
                        subPrompt = "Mental multiplication & division",
                        options = options,
                        correctIndex = options.indexOf(correct.toString()),
                        explanation = if (isDiv) "$ans ÷ $a = $b" else "$a × $b = $ans",
                        timeLimitSeconds = 8,
                        visualType = VisualType.MATH_OPERATORS
                    )
                }
            }
            else -> {
                // Complex operations, brackets, combinations
                val a = rand.nextInt(3, 12 + level)
                val b = rand.nextInt(2, 9)
                val c = rand.nextInt(5, 25 + level)
                val op1 = listOf("+", "-", "×").random(rand)
                val op2 = listOf("+", "-").random(rand)

                val intermediate = when (op1) {
                    "+" -> a + b
                    "-" -> a - b
                    else -> a * b
                }
                val ans = when (op2) {
                    "+" -> intermediate + c
                    else -> intermediate - c
                }

                val prompt = "($a $op1 $b) $op2 $c = ?"
                val options = generateDistractors(ans, 4, rand)
                GameQuestion(
                    id = "math_${level}_$qIndex",
                    prompt = prompt,
                    subPrompt = "Evaluate the formula!",
                    options = options,
                    correctIndex = options.indexOf(ans.toString()),
                    explanation = "($a $op1 $b) = $intermediate, then $intermediate $op2 $c = $ans",
                    timeLimitSeconds = maxOf(7, 12 - (level / 15)),
                    visualType = VisualType.MATH_OPERATORS
                )
            }
        }
    }

    private fun generateDistractors(correct: Int, count: Int, rand: Random): List<String> {
        val set = mutableSetOf(correct)
        var offset = 1
        while (set.size < count) {
            val delta = (rand.nextInt(1, 4) * (if (rand.nextBoolean()) 1 else -1)) * offset
            val cand = correct + delta
            if (cand != correct) set.add(cand)
            offset++
        }
        return set.shuffled(rand).map { it.toString() }
    }

    // --- 2. PATTERN MATRIX GENERATOR ---
    private fun generateMatrixQuestion(level: Int, qIndex: Int): GameQuestion {
        val rand = Random(System.nanoTime() + level * 2000 + qIndex)
        val gridSize = when {
            level <= 4 -> 3
            level <= 12 -> 3
            level <= 25 -> 4
            else -> 4
        }
        val totalCells = gridSize * gridSize
        val flashCount = when {
            level <= 3 -> 3
            level <= 8 -> 4
            level <= 15 -> 5
            else -> minOf(7, 4 + (level / 5))
        }

        val allIndices = (0 until totalCells).toList().shuffled(rand)
        val targetIndices = allIndices.take(flashCount)
        val targetSequenceStr = targetIndices.joinToString(",")

        // Question format: either identify the total flashed or tap the grid
        val displayTime = maxOf(1200L, 2600L - (level * 50L))

        return GameQuestion(
            id = "matrix_${level}_$qIndex",
            prompt = "Remember the active glowing cells!",
            subPrompt = "Memorize the $flashCount highlighted positions.",
            options = listOf("Ready", "Echo", "Clear", "Verify"),
            correctIndex = 0,
            explanation = "Highlighted cells were at positions: ${targetIndices.map { it + 1 }.joinToString(", ")}",
            timeLimitSeconds = 15,
            visualType = VisualType.MATRIX_GRID,
            metadata = mapOf(
                "gridSize" to gridSize,
                "targetIndices" to targetIndices,
                "displayDurationMs" to displayTime,
                "flashCount" to flashCount
            )
        )
    }

    // --- 3. STROOP FOCUS GENERATOR ---
    private val colorPalettes = listOf(
        Triple("RED", 0xFFFF3B30, "Red"),
        Triple("BLUE", 0xFF007AFF, "Blue"),
        Triple("GREEN", 0xFF34C759, "Green"),
        Triple("YELLOW", 0xFFFFCC00, "Yellow"),
        Triple("PURPLE", 0xFFAF52DE, "Purple"),
        Triple("ORANGE", 0xFFFF9500, "Orange"),
        Triple("CYAN", 0xFF32D74B, "Cyan"),
        Triple("PINK", 0xFFFF2D55, "Pink")
    )

    private fun generateStroopQuestion(level: Int, qIndex: Int): GameQuestion {
        val rand = Random(System.nanoTime() + level * 3000 + qIndex)
        val wordPair = colorPalettes.random(rand)
        // Ensure some match and some conflict
        val shouldConflict = rand.nextFloat() > 0.35f
        val colorPair = if (shouldConflict) {
            colorPalettes.filter { it.first != wordPair.first }.random(rand)
        } else {
            wordPair
        }

        val rule = if (rand.nextBoolean()) StroopRule.MATCH_INK_COLOR else StroopRule.MATCH_WORD_NAME
        val promptRuleText = when (rule) {
            StroopRule.MATCH_INK_COLOR -> "Select the actual INK COLOR of the word!"
            StroopRule.MATCH_WORD_NAME -> "Select the TEXT WORD itself (ignore the ink color)!"
            StroopRule.MATCH_BACKGROUND_CONFLICT -> "Match color attribute!"
        }

        val correctColorName = when (rule) {
            StroopRule.MATCH_INK_COLOR -> colorPair.third
            StroopRule.MATCH_WORD_NAME -> wordPair.third
            else -> colorPair.third
        }

        val optionList = mutableSetOf(correctColorName)
        while (optionList.size < 4) {
            optionList.add(colorPalettes.random(rand).third)
        }
        val options = optionList.shuffled(rand).toList()
        val correctIndex = options.indexOf(correctColorName)

        return GameQuestion(
            id = "stroop_${level}_$qIndex",
            prompt = wordPair.first,
            subPrompt = promptRuleText,
            options = options,
            correctIndex = correctIndex,
            explanation = "Word displayed was '${wordPair.first}' painted in ${colorPair.third} ink.",
            timeLimitSeconds = maxOf(4, 9 - (level / 8)),
            visualType = VisualType.STROOP_CARD,
            metadata = mapOf(
                "textColorHex" to colorPair.second,
                "wordText" to wordPair.first,
                "ruleType" to rule.name
            )
        )
    }

    // --- 4. LOGIC RIDDLES & PATTERNS ---
    private val curatedLogicRiddles = listOf(
        Triple(
            "What has keys but no locks, space but no room, and you can enter but never go inside?",
            listOf("Keyboard", "Piano", "Map", "Clock"),
            "A Keyboard has keys, a space bar, and an enter key!"
        ),
        Triple(
            "Find the next number in the pattern: 2, 6, 12, 20, 30, ?",
            listOf("42", "40", "36", "48"),
            "Differences increase by 2: +4, +6, +8, +10, so +12 -> 30 + 12 = 42."
        ),
        Triple(
            "A bat and a ball cost $1.10 in total. The bat costs $1.00 more than the ball. How much is the ball?",
            listOf("$0.05", "$0.10", "$0.15", "$0.01"),
            "Ball = $0.05, Bat = $1.05 -> Total = $1.10."
        ),
        Triple(
            "If five cats catch five mice in five minutes, how many minutes will it take 100 cats to catch 100 mice?",
            listOf("5 minutes", "100 minutes", "1 minute", "20 minutes"),
            "Each cat takes 5 minutes to catch 1 mouse, so 100 cats take 5 minutes for 100 mice."
        ),
        Triple(
            "Next in sequence: 1, 1, 2, 3, 5, 8, 13, ?",
            listOf("21", "18", "20", "24"),
            "Fibonacci series: each number is the sum of the two preceding numbers (8 + 13 = 21)."
        ),
        Triple(
            "I speak without a mouth and hear without ears. I have no body, but I come alive with wind. What am I?",
            listOf("An Echo", "A Cloud", "A Shadow", "A Whisper"),
            "An echo reflects sound through the air without physical body."
        ),
        Triple(
            "Which number is missing in the series: 3, 9, 27, 81, ?",
            listOf("243", "162", "216", "324"),
            "Each number is multiplied by 3 (81 × 3 = 243)."
        ),
        Triple(
            "If tomorrow was yesterday, today would be as far from Sunday as today is from Sunday if yesterday was tomorrow. What day is it?",
            listOf("Sunday", "Wednesday", "Friday", "Monday"),
            "Sunday is the symmetry axis point."
        ),
        Triple(
            "How many triangles are formed by dividing a square with both its diagonals?",
            listOf("8", "4", "6", "10"),
            "4 small single triangles + 4 composite double triangles = 8 total triangles."
        ),
        Triple(
            "Look at this sequence: J, F, M, A, M, J, J, A, S, O, N, ?",
            listOf("D", "E", "P", "L"),
            "First letters of months in a year: January through December -> 'D'."
        ),
        Triple(
            "A plane crashes on the border of the US and Canada. Where do they bury the survivors?",
            listOf("Nowhere (they survived!)", "Canada", "United States", "Border line"),
            "You do not bury survivors!"
        ),
        Triple(
            "Find the odd one out among these 3D solids:",
            listOf("Cylinder", "Cube", "Tetrahedron", "Octahedron"),
            "Cylinder has curved surface, whereas the others are polyhedra with planar faces."
        ),
        Triple(
            "If 1 = 3, 2 = 3, 3 = 5, 4 = 4, 5 = 4, what does 6 equal?",
            listOf("3", "6", "4", "5"),
            "It counts letters in the English word for each number: 'SIX' has 3 letters."
        ),
        Triple(
            "A doctor gives you 3 pills and tells you to take one every half hour. How long will the pills last?",
            listOf("60 minutes", "90 minutes", "30 minutes", "120 minutes"),
            "1st pill at 0m, 2nd pill at 30m, 3rd pill at 60m -> 60 minutes total."
        )
    )

    private fun generateLogicQuestion(level: Int, qIndex: Int): GameQuestion {
        val rand = Random(System.nanoTime() + level * 4000 + qIndex)
        // Select from curated pool or generate procedural sequence
        val isSequence = rand.nextBoolean() || level > 14

        if (!isSequence && curatedLogicRiddles.isNotEmpty()) {
            val item = curatedLogicRiddles[(level * 3 + qIndex) % curatedLogicRiddles.size]
            val correctOption = item.second[0]
            val shuffledOptions = item.second.shuffled(rand)
            val correctIndex = shuffledOptions.indexOf(correctOption)

            return GameQuestion(
                id = "logic_${level}_$qIndex",
                prompt = item.first,
                subPrompt = "Deductive & Lateral Reasoning",
                options = shuffledOptions,
                correctIndex = correctIndex,
                explanation = item.third,
                timeLimitSeconds = 18,
                visualType = VisualType.LOGIC_ILLUSTRATION
            )
        } else {
            // Algorithmic number / symbol sequence puzzle
            val start = rand.nextInt(2, 10 + level)
            val step = rand.nextInt(2, 6 + (level / 4))
            val isMult = rand.nextFloat() > 0.65f && step <= 4

            val seq = mutableListOf<Int>()
            var curr = start
            for (i in 0..4) {
                seq.add(curr)
                curr = if (isMult) curr * 2 else curr + step
            }
            val answer = curr
            val prompt = "Find the missing sequence number: ${seq.joinToString(", ")}, [ ? ]"
            val options = generateDistractors(answer, 4, rand)
            val correctIndex = options.indexOf(answer.toString())

            return GameQuestion(
                id = "logic_seq_${level}_$qIndex",
                prompt = prompt,
                subPrompt = if (isMult) "Exponential doubling sequence" else "Arithmetic progression puzzle",
                options = options,
                correctIndex = correctIndex,
                explanation = if (isMult) "Each term multiplies by 2: next is $answer" else "Common difference is +$step: next is $answer",
                timeLimitSeconds = 14,
                visualType = VisualType.LOGIC_ILLUSTRATION
            )
        }
    }

    // --- 5. WORD SPARKS & ANAGRAM GENERATOR ---
    private val curatedWords = listOf(
        Pair("SYNAPSE", "Connection junction between brain neurons"),
        Pair("MEMORY", "Ability to store and recall experiences"),
        Pair("LOGIC", "System of valid reasoning and deduction"),
        Pair("FOCUS", "Concentration of mental energy on a single task"),
        Pair("QUANTUM", "Smallest discrete unit of energy or state"),
        Pair("NEXUS", "A central link or intersection in a network"),
        Pair("INSIGHT", "Sudden deep understanding of a problem"),
        Pair("CIPHER", "A secret code or cryptographic system"),
        Pair("MATRIX", "A rectangular array or grid structure"),
        Pair("COGNITION", "Mental action of acquiring knowledge"),
        Pair("CORTEX", "Outer layer of neural tissue in the brain"),
        Pair("REFLEX", "Rapid involuntary response to stimulus"),
        Pair("GENIUS", "Exceptional intellectual or creative ability"),
        Pair("WISDOM", "Quality of having experience, knowledge, and good judgment"),
        Pair("SPARK", "A trace of brilliant creative ignition"),
        Pair("PUZZLE", "A game designed to test ingenuity or knowledge"),
        Pair("INFINITY", "State of being boundless or endless"),
        Pair("CLARITY", "State of mental coherence and precision"),
        Pair("ENIGMA", "A mysterious or perplexing person or thing"),
        Pair("VELOCITY", "Speed of operation in a given direction"),
        Pair("ALGORITHM", "Step-by-step procedure for calculation")
    )

    private fun generateWordQuestion(level: Int, qIndex: Int): GameQuestion {
        val rand = Random(System.nanoTime() + level * 5000 + qIndex)
        val selected = curatedWords[(level * 2 + qIndex) % curatedWords.size]
        val word = selected.first
        val hint = selected.second

        // Scramble letters
        var scrambled = word.toList().shuffled(rand)
        while (scrambled.joinToString("") == word && word.length > 2) {
            scrambled = word.toList().shuffled(rand)
        }

        // Generate 4 candidate anagram words
        val candidates = mutableListOf(word)
        // create smart anagram distractors with swapped letters
        for (k in 1..3) {
            val chars = word.toCharArray()
            if (chars.size >= 4) {
                val i1 = rand.nextInt(chars.size)
                val i2 = rand.nextInt(chars.size)
                val temp = chars[i1]
                chars[i1] = chars[i2]
                chars[i2] = temp
            }
            var candStr = String(chars)
            if (candStr == word) candStr = word.reversed()
            if (!candidates.contains(candStr)) candidates.add(candStr)
        }
        // Fill up if needed
        while (candidates.size < 4) {
            candidates.add(curatedWords.random(rand).first)
        }

        val options = candidates.shuffled(rand)
        val correctIndex = options.indexOf(word)

        return GameQuestion(
            id = "word_${level}_$qIndex",
            prompt = scrambled.joinToString("  "),
            subPrompt = "Unscramble the letters to form the correct word!",
            options = options,
            correctIndex = correctIndex,
            explanation = "$word: $hint",
            timeLimitSeconds = 15,
            visualType = VisualType.WORD_ANAGRAM,
            metadata = mapOf(
                "targetWord" to word,
                "scrambledLetters" to scrambled.joinToString(""),
                "hintDefinition" to hint
            )
        )
    }
}
