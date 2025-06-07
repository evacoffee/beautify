document.addEventListener('DOMContentLoaded', () => {
    // Theme toggle functionality
    const themeToggle = document.querySelector('.theme-toggle');
    const body = document.body;

    // Check for saved theme preference
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) {
        body.setAttribute('data-theme', savedTheme);
        updateThemeIcon(savedTheme);
    }

    themeToggle.addEventListener('click', () => {
        const currentTheme = body.getAttribute('data-theme') || 'light';
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';
        
        body.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        updateThemeIcon(newTheme);
    });

    function updateThemeIcon(theme) {
        const icon = themeToggle.querySelector('i');
        icon.className = theme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
    }

    // Add smooth scroll to navigation links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            document.querySelector(this.getAttribute('href')).scrollIntoView({
                behavior: 'smooth'
            });
        });
    });

    // Initialize routine cards
    initializeRoutineCards();
    initializeTutorialCards();
    initializeReviewCards();
});

// Routine Cards Data
const routineData = [
    {
        title: 'Daily Morning Routine',
        description: 'A gentle morning skincare routine to start your day fresh and glowing.',
        products: ['Cleanser', 'Toner', 'Serum', 'Moisturizer', 'Sunscreen']
    },
    {
        title: 'Nighttime Recovery Routine',
        description: 'Deep hydration and repair for overnight skin restoration.',
        products: ['Cleanser', 'Exfoliant', 'Retinol', 'Moisturizer']
    },
    {
        title: 'Sensitive Skin Routine',
        description: 'Gentle, non-irritating products for sensitive skin types.',
        products: ['Gentle Cleanser', 'Soothing Serum', 'Hydrating Moisturizer']
    }
];

// Tutorial Cards Data
const tutorialData = [
    {
        title: 'Foundation Application',
        video: 'foundation.mp4',
        description: 'Learn how to apply foundation for a natural, flawless look.'
    },
    {
        title: 'Eye Makeup Basics',
        video: 'eye-makeup.mp4',
        description: 'Master the basics of eye makeup application.'
    },
    {
        title: 'Contouring Techniques',
        video: 'contouring.mp4',
        description: 'Learn professional contouring techniques for a sculpted look.'
    }
];

// Review Cards Data
const reviewData = [
    {
        name: 'Emma S.',
        rating: 5,
        product: 'Hydrating Serum',
        review: 'This serum has completely transformed my dry skin! Highly recommend!'
    },
    {
        name: 'Sophie R.',
        rating: 4,
        product: 'Night Cream',
        review: 'Love the lightweight texture and how it makes my skin feel in the morning.'
    },
    {
        name: 'Lily W.',
        rating: 5,
        product: 'Face Mask',
        review: 'Best face mask I\'ve ever used! My skin feels so soft and hydrated.'
    }
];

// Initialize Routine Cards
function initializeRoutineCards() {
    const routineGrid = document.querySelector('.routine-grid');
    if (!routineGrid) return;

    routineData.forEach(routine => {
        const card = document.createElement('div');
        card.className = 'routine-card';
        card.innerHTML = `
            <div class="routine-card-content">
                <h3>${routine.title}</h3>
                <p>${routine.description}</p>
                <ul class="products-list">
                    ${routine.products.map(product => `<li>${product}</li>`).join('')}
                </ul>
                <button class="btn-outline">Try Routine</button>
            </div>
        `;
        routineGrid.appendChild(card);
    });
}

// Initialize Tutorial Cards
function initializeTutorialCards() {
    const tutorialGrid = document.querySelector('.tutorial-grid');
    if (!tutorialGrid) return;

    tutorialData.forEach(tutorial => {
        const card = document.createElement('div');
        card.className = 'tutorial-card';
        card.innerHTML = `
            <video src="videos/${tutorial.video}" muted></video>
            <div class="tutorial-card-content">
                <h3>${tutorial.title}</h3>
                <p>${tutorial.description}</p>
                <button class="btn-outline">Watch Tutorial</button>
            </div>
        `;
        tutorialGrid.appendChild(card);
    });
}

// Initialize Review Cards
function initializeReviewCards() {
    const reviewsGrid = document.querySelector('.reviews-grid');
    if (!reviewsGrid) return;

    reviewData.forEach(review => {
        const card = document.createElement('div');
        card.className = 'review-card';
        card.innerHTML = `
            <div class="review-header">
                <img src="images/avatars/${review.name.toLowerCase().split(' ')[0][0]}.jpg" alt="${review.name}">
                <div class="review-info">
                    <h4>${review.name}</h4>
                    <div class="rating">
                        ${Array(review.rating).fill('⭐').join('')}
                    </div>
                </div>
            </div>
            <p>${review.review}</p>
            <div class="reviewer">${review.product}</div>
        `;
        reviewsGrid.appendChild(card);
    });
}

// Skin Analysis Functionality
const uploadArea = document.querySelector('.upload-area');
if (uploadArea) {
    const fileInput = uploadArea.querySelector('input[type="file"]');
    
    uploadArea.addEventListener('click', () => {
        fileInput.click();
    });

    fileInput.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
            analyzeSkin(file);
        }
    });
}

async function analyzeSkin(file) {
    // TODO: Implement AI skin analysis API call
    // This is a placeholder for the actual implementation
    const result = {
        skinType: 'Combination',
        concerns: ['Dryness', 'Acne', 'Dullness'],
        recommendations: [
            'Hydrating Serum',
            'Gentle Cleanser',
            'Oil-Free Moisturizer'
        ]
    };

    displayAnalysisResults(result);
}

function displayAnalysisResults(result) {
    const analysisSection = document.querySelector('.analysis-results');
    if (!analysisSection) return;

    analysisSection.innerHTML = `
        <h3>Your Skin Analysis Results</h3>
        <div class="skin-type">
            <h4>Skin Type</h4>
            <div class="type-badge">${result.skinType}</div>
        </div>
        <div class="concerns">
            <h4>Concerns</h4>
            <ul>
                ${result.concerns.map(concern => `<li>${concern}</li>`).join('')}
            </ul>
        </div>
        <div class="recommendations">
            <h4>Recommended Products</h4>
            <ul>
                ${result.recommendations.map(product => `<li>${product}</li>`).join('')}
            </ul>
        </div>
    `;
}
