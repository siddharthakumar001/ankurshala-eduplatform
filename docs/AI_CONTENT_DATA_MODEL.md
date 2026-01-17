# Course Content Data Model for AI Integration

## Overview

This document describes the complete course content data model designed for AI integration in the Ankurshala educational platform. The structure supports hierarchical taxonomy, relationships, and rich metadata for intelligent content recommendations and personalized learning.

## 📊 Complete Data Structure

### 1. Hierarchical Taxonomy

```
Board (CBSE, Bihar Board, ICSE, State Board)
  └── Grade (1-12)
      └── Subject (Mathematics, Physics, Chemistry, etc.)
          └── Chapter (e.g., "Number Systems", "Plant Nutrition")
              └── Topic (e.g., "Whole numbers", "Photosynthesis")
                  ├── Description (Detailed explanation)
                  ├── Summary (Brief overview)
                  ├── Suggested Topics (Related content)
                  ├── Duration (Expected learning time)
                  ├── Notes (Rich content with attachments)
                  └── Links (Prerequisites & Related topics)
```

### 2. Database Schema

#### **topics** table
| Column | Type | Description | AI Usage |
|--------|------|-------------|----------|
| `id` | BIGSERIAL | Primary key | Entity identification |
| `title` | VARCHAR(300) | Topic name | Search, recommendations |
| `code` | VARCHAR(100) | Unique identifier | Quick reference |
| **`description`** | **TEXT** | **Detailed explanation** | **Content understanding, NLP** |
| **`summary`** | **TEXT** | **Brief overview** | **Quick context, embeddings** |
| **`suggested_topics`** | **TEXT** | **Related topics list** | **Recommendations engine** |
| **`expected_time_mins`** | **INTEGER** | **Learning duration** | **Time planning, pacing** |
| `chapter_id` | BIGINT | Parent chapter | Hierarchy navigation |
| `subject_id` | BIGINT | Subject reference | Content filtering |
| `board_id` | BIGINT | Board reference | Curriculum-specific |
| `grade_id` | BIGINT | Grade level | Age-appropriate content |
| `active` | BOOLEAN | Visibility | Content curation |
| `created_at` | TIMESTAMP | Creation time | Versioning |
| `updated_at` | TIMESTAMP | Last modified | Change tracking |

#### **topic_links** table (Relationships)
| Column | Type | Description | AI Usage |
|--------|------|-------------|----------|
| `id` | BIGSERIAL | Primary key | - |
| `topic_id` | BIGINT | Source topic | Learning paths |
| `type` | VARCHAR(20) | PREREQUISITE / RELATED | Dependency graph |
| `linked_topic_id` | BIGINT | Target topic | Knowledge graph |

#### **topic_notes** table (Rich Content)
| Column | Type | Description | AI Usage |
|--------|------|-------------|----------|
| `id` | BIGSERIAL | Primary key | - |
| `topic_id` | BIGINT | Parent topic | Content association |
| `title` | VARCHAR(300) | Note title | Subtopic identification |
| `content` | TEXT | Note content | Deep learning resources |
| `attachments` | JSONB | Files, URLs | Multimedia learning |

## 🤖 AI Integration Use Cases

### 1. Content Understanding & Embeddings

**Fields Used:** `title`, `description`, `summary`

```python
# Generate embeddings for semantic search
topic_embedding = model.encode(
    f"{topic.title}. {topic.description}. {topic.summary}"
)

# Store in vector database for similarity search
vector_db.upsert(topic.id, topic_embedding)
```

**Use Cases:**
- Semantic search across topics
- Similar content discovery
- Content clustering
- Duplicate detection

### 2. Personalized Recommendations

**Fields Used:** `suggested_topics`, `topic_links`, `expected_time_mins`

```python
# Get next recommended topics
def get_recommendations(student_id, current_topic_id):
    # 1. Check suggested topics
    suggested = Topic.get(current_topic_id).suggested_topics.split(',')
    
    # 2. Check related links
    related = TopicLink.filter(
        topic_id=current_topic_id,
        type='RELATED'
    )
    
    # 3. Filter by student preferences
    recommended = filter_by_student_preferences(
        topics=suggested + related,
        student_id=student_id
    )
    
    return recommended
```

**Use Cases:**
- "What to learn next?" suggestions
- Personalized learning paths
- Adaptive content sequencing
- Gap analysis

### 3. Learning Time Estimation

**Fields Used:** `expected_time_mins`, `topic_links`

```python
# Estimate learning path duration
def estimate_learning_path(topic_ids):
    total_time = 0
    prerequisites = []
    
    for topic_id in topic_ids:
        topic = Topic.get(topic_id)
        total_time += topic.expected_time_mins
        
        # Add prerequisite time
        prereqs = TopicLink.filter(
            topic_id=topic_id,
            type='PREREQUISITE'
        )
        prerequisites.extend(prereqs)
    
    return {
        'direct_time': total_time,
        'with_prerequisites': total_time + calculate_prereq_time(prerequisites)
    }
```

**Use Cases:**
- Study session planning
- Course duration estimates
- Learning pace tracking
- Time-based recommendations

### 4. Knowledge Graph Construction

**Fields Used:** All topic fields + `topic_links`

```python
# Build knowledge graph
def build_knowledge_graph():
    graph = nx.DiGraph()
    
    # Add nodes
    for topic in Topic.all():
        graph.add_node(
            topic.id,
            title=topic.title,
            description=topic.description,
            summary=topic.summary,
            duration=topic.expected_time_mins
        )
    
    # Add edges (prerequisites and related)
    for link in TopicLink.all():
        graph.add_edge(
            link.topic_id,
            link.linked_topic_id,
            type=link.type
        )
    
    return graph
```

**Use Cases:**
- Learning path visualization
- Prerequisite validation
- Curriculum design
- Gap identification

### 5. Content Generation & Enhancement

**Fields Used:** `description`, `summary`, `suggested_topics`

```python
# AI-enhance topic content
def enhance_topic_content(topic_id):
    topic = Topic.get(topic_id)
    
    # Generate better description if empty
    if not topic.description:
        topic.description = ai_model.generate_description(
            title=topic.title,
            subject=topic.subject.name,
            grade=topic.grade.name
        )
    
    # Generate summary if empty
    if not topic.summary:
        topic.summary = ai_model.summarize(topic.description)
    
    # Generate suggested topics
    if not topic.suggested_topics:
        similar = find_similar_topics(topic)
        topic.suggested_topics = ','.join([t.title for t in similar])
    
    topic.save()
```

**Use Cases:**
- Auto-complete missing content
- Content quality improvement
- Consistency checking
- Translation

### 6. Intelligent Search

**Fields Used:** `title`, `description`, `summary`, `suggested_topics`

```python
# Multi-field search with AI ranking
def intelligent_search(query, student_preferences):
    # 1. Keyword search
    keyword_results = search_topics(
        query,
        fields=['title', 'description', 'summary', 'suggested_topics']
    )
    
    # 2. Semantic search
    query_embedding = model.encode(query)
    semantic_results = vector_db.search(query_embedding, top_k=20)
    
    # 3. Combine and rank
    combined = merge_results(keyword_results, semantic_results)
    
    # 4. Personalize based on student context
    personalized = rank_by_preferences(combined, student_preferences)
    
    return personalized
```

**Use Cases:**
- Smart topic discovery
- Context-aware search
- Question answering
- Content discovery

## 📝 Data Population Examples

### Example 1: Complete Topic Entry

```json
{
  "id": 1,
  "title": "Photosynthesis",
  "code": "BIO_PLT_PHO_1",
  "description": "Photosynthesis is the process by which green plants use sunlight, water, and carbon dioxide to create oxygen and energy in the form of sugar. This fundamental process is essential for life on Earth as it provides oxygen and serves as the primary source of energy for most living organisms.",
  "summary": "Process where plants convert light energy into chemical energy (sugar) using water, CO2, and chlorophyll.",
  "suggested_topics": "Chloroplasts, Light reactions, Calvin cycle, Cellular respiration, Plant nutrition",
  "expected_time_mins": 90,
  "chapter_id": 42,
  "subject_id": 5,
  "board_id": 1,
  "grade_id": 10,
  "active": true
}
```

### Example 2: With Relationships

```json
{
  "topic": {
    "id": 1,
    "title": "Photosynthesis",
    "description": "...",
    "summary": "...",
    "suggested_topics": "Chloroplasts, Light reactions, Calvin cycle",
    "expected_time_mins": 90
  },
  "prerequisites": [
    {
      "topic_id": 1,
      "linked_topic_id": 38,
      "type": "PREREQUISITE",
      "linked_topic_title": "Cell Structure"
    },
    {
      "topic_id": 1,
      "linked_topic_id": 39,
      "type": "PREREQUISITE",
      "linked_topic_title": "Plant Cells"
    }
  ],
  "related_topics": [
    {
      "topic_id": 1,
      "linked_topic_id": 45,
      "type": "RELATED",
      "linked_topic_title": "Cellular Respiration"
    }
  ]
}
```

### Example 3: For AI Training

```python
# Format for AI model training
training_data = {
    "topic_id": 1,
    "features": {
        # Text features
        "title": "Photosynthesis",
        "description": "...",
        "summary": "...",
        "suggested_topics": ["Chloroplasts", "Light reactions", "Calvin cycle"],
        
        # Numeric features
        "expected_time_mins": 90,
        "grade_level": 10,
        
        # Categorical features
        "subject": "Biology",
        "board": "CBSE",
        "chapter": "Plant Nutrition",
        
        # Relationship features
        "prerequisite_count": 2,
        "related_count": 1,
        "depth_in_tree": 5
    },
    "labels": {
        "difficulty": "medium",
        "popularity_score": 0.85,
        "completion_rate": 0.78
    }
}
```

## 🎯 Field Guidelines for Content Creators

### **Description** (TEXT)
- **Purpose:** Detailed explanation of the topic
- **Length:** 200-500 words
- **Content:**
  - Define the concept clearly
  - Explain key principles
  - Provide context and real-world applications
  - Include important terminology
- **AI Value:** High - used for embeddings, NLP, comprehension

**Example:**
```
Photosynthesis is the biochemical process by which green plants, algae, 
and some bacteria convert light energy (usually from the sun) into chemical 
energy stored in glucose molecules. This process occurs primarily in the 
chloroplasts of plant cells...
```

### **Summary** (TEXT)
- **Purpose:** Brief, concise overview
- **Length:** 1-3 sentences (50-150 words)
- **Content:**
  - Core concept in plain language
  - Key takeaway
  - Essential definition
- **AI Value:** High - used for quick understanding, embeddings

**Example:**
```
Plants use sunlight, water, and CO2 to produce glucose and oxygen through 
photosynthesis. This process occurs in chloroplasts and is essential for 
life on Earth.
```

### **Suggested Topics** (TEXT)
- **Purpose:** Related topics for further learning
- **Format:** Comma-separated list or JSON array
- **Content:**
  - Related concepts
  - Follow-up topics
  - Alternative perspectives
  - Prerequisite reminders
- **AI Value:** High - used for recommendations, learning paths

**Example:**
```
Chloroplasts structure, Light-dependent reactions, Calvin cycle, 
Cellular respiration, ATP synthesis, Plant cell organelles
```

Or JSON format:
```json
[
  "Chloroplasts structure",
  "Light-dependent reactions",
  "Calvin cycle",
  "Cellular respiration"
]
```

### **Expected Time (Minutes)** (INTEGER)
- **Purpose:** Estimated learning duration
- **Range:** 15-180 minutes typically
- **Guidelines:**
  - Reading time: ~250 words/minute
  - Include practice time
  - Add buffer for comprehension
- **AI Value:** Medium - used for planning, pacing

**Examples:**
- Simple concept: 30 minutes
- Medium concept: 60 minutes
- Complex concept: 90-120 minutes
- In-depth topic: 180+ minutes

## 🔄 Data Migration

If you have existing content without these fields, here's how to populate them:

### Using AI to Generate Content

```python
# Script to populate missing fields
from openai import OpenAI

client = OpenAI(api_key=os.getenv('OPENAI_API_KEY'))

def populate_missing_fields():
    topics = Topic.filter(Q(description__isnull=True) | Q(summary__isnull=True))
    
    for topic in topics:
        # Generate description
        if not topic.description:
            topic.description = generate_description(topic)
        
        # Generate summary
        if not topic.summary:
            topic.summary = generate_summary(topic)
        
        # Generate suggested topics
        if not topic.suggested_topics:
            topic.suggested_topics = generate_suggestions(topic)
        
        # Estimate time
        if not topic.expected_time_mins:
            topic.expected_time_mins = estimate_time(topic)
        
        topic.save()

def generate_description(topic):
    prompt = f"""
    Generate a detailed educational description for the topic: {topic.title}
    Subject: {topic.subject.name}
    Grade: {topic.grade.display_name}
    Chapter: {topic.chapter.name}
    
    The description should:
    - Be 200-300 words
    - Explain the concept clearly
    - Include real-world applications
    - Be appropriate for grade level
    """
    
    response = client.chat.completions.create(
        model="gpt-4",
        messages=[{"role": "user", "content": prompt}]
    )
    
    return response.choices[0].message.content
```

## 📈 Analytics & Insights

With complete content data, you can generate powerful insights:

### Content Quality Metrics
```sql
-- Topics with complete metadata
SELECT 
    COUNT(*) FILTER (WHERE description IS NOT NULL AND summary IS NOT NULL) as complete,
    COUNT(*) as total,
    ROUND(100.0 * COUNT(*) FILTER (WHERE description IS NOT NULL AND summary IS NOT NULL) / COUNT(*), 2) as completion_rate
FROM topics;
```

### Learning Time Analytics
```sql
-- Average learning time by subject
SELECT 
    s.name as subject,
    AVG(t.expected_time_mins) as avg_time_mins,
    COUNT(t.id) as topic_count
FROM topics t
JOIN subjects s ON t.subject_id = s.id
WHERE t.expected_time_mins IS NOT NULL
GROUP BY s.name
ORDER BY avg_time_mins DESC;
```

### Content Relationship Density
```sql
-- Topics with most relationships
SELECT 
    t.title,
    t.suggested_topics,
    COUNT(tl.id) as relationship_count
FROM topics t
LEFT JOIN topic_links tl ON t.id = tl.topic_id
GROUP BY t.id, t.title, t.suggested_topics
ORDER BY relationship_count DESC
LIMIT 10;
```

## ✅ Implementation Checklist

- [x] Database schema created (topics table)
- [x] `description` field (TEXT) - ✅ Already exists
- [x] `summary` field (TEXT) - ✅ Already exists
- [x] `suggested_topics` field (TEXT) - ✅ Added in V37 migration
- [x] `expected_time_mins` field (INTEGER) - ✅ Already exists
- [x] Topic entity updated
- [x] DTO updated
- [x] Migration script created (V37)
- [ ] API endpoints support new field
- [ ] Frontend displays new fields
- [ ] Admin panel for content editing
- [ ] AI integration scripts
- [ ] Content population scripts
- [ ] Analytics dashboard

## 🚀 Next Steps

1. **Run Migration:** Apply V37 migration to add `suggested_topics` field
2. **Update Services:** Ensure import services populate all fields
3. **Create Admin UI:** Build forms for content editing
4. **Populate Data:** Use AI to fill missing content
5. **Build AI Features:** Implement recommendation engine
6. **Test & Validate:** Verify content quality
7. **Monitor Usage:** Track field utilization

---

**Last Updated:** January 15, 2026  
**Migration Version:** V37__add_suggested_topics_to_topics.sql
