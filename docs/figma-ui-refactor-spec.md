# Memory Helper UI Refactor Spec

Figma MCP is available in the current environment, but there is no target design file or node link yet. This document defines the design system and screen structure that now exist in code, and can be translated into a Figma file once a target file is provided.

## Visual Direction

- Theme: editorial study dashboard
- Mood: focused, warm, deliberate
- Primary accent: cobalt
- Secondary accent: teal
- Surfaces: warm paper tones instead of flat white
- Shape language: rounded but not bubbly, with larger hero radii than utility controls

## Design Tokens

### Core Colors

- Primary: `#2453FF`
- Primary Dark: `#1738B8`
- Secondary: `#0F8A82`
- Success: `#26845B`
- Warning: `#D48A1B`
- Error: `#D94A34`
- Background: `#F6F1E8`
- Surface: `#FFFBF5`
- Surface Variant: `#E9E2D6`
- Text Primary: `#1E1B17`
- Text Secondary: `#5E584F`

### Typography

- Display / Headline: serif family, bold or semibold
- Body / Label: sans-serif family
- Hero size: `displaySmall`
- Section titles: `titleLarge`
- Supporting copy: `bodySmall`

### Shapes

- Utility controls: `10-14dp`
- Standard cards: `20dp`
- Hero cards: `28-36dp`

## Component Inventory

### App Card

- `Surface`: neutral container with border
- `Elevated`: utility card for metrics or nested content
- `Accent`: hero card for plans, entry points, and high-priority actions

### Header Stat Chip

- Label
- Numeric value
- Used in Home hero and Exam metrics

### Mini Pill

- Compact tag for state, weight, or status
- Used in Exam rows and future plan chips

### Progress Ring

- Circular snapshot used only in hero contexts
- Should not be reused inside dense list rows

## Page Structure

### Home

- Hero header
- Optional exam plan banner
- Daily progress summary
- Notebook filters
- Start review CTA
- Sectioned queue: Overdue / Today / Upcoming / Completed

### Exam

- Active plan hero
- Subject configuration block
- Today plan block
- Dialog entry points for create plan and add subject

### Stats

- 7-day summary hero
- Study signals metrics
- Plan completion block
- Review chart
- Grade distribution

### Flashcard

- Immersive review mode
- Front: single prompt focus
- Back: answer + media
- Four-grade controls

## Figma File Outline

### Pages

1. `Foundations`
2. `Components`
3. `Home`
4. `Exam`
5. `Stats`
6. `Flashcard`

### Foundations Frame Set

- Colors
- Typography
- Spacing
- Radius
- Shadows

### Components Frame Set

- App Card variants
- Buttons
- Filter chips
- Hero header
- Stat chip
- Plan mini pill
- Progress ring
- Review action buttons

## Current Figma Artifact

- FigJam map: created from this refactor pass to capture foundations, components, and screen coverage
- Use it as the starting board before turning the system into production Figma frames

## Interaction Notes

- Hero cards should feel static and readable, not overly animated
- Review interactions remain the most animated surface
- Search and import actions should read as utilities, not primary narrative blocks
- Overdue content should always outrank upcoming content visually and structurally

## Current Implementation Mapping

- Theme tokens: `ui/theme/*`
- Design system: `ui/designsystem/*`
- Home: `ui/screens/home/HomeScreen.kt`
- Exam: `ui/screens/exam/ExamScreen.kt`
- Stats: `ui/screens/stats/StatsScreen.kt`
- Flashcard: `ui/screens/flashcard/FlashcardScreen.kt`
