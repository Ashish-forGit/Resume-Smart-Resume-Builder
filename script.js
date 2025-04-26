document.addEventListener('DOMContentLoaded', function() {
    // Add smooth scrolling to sections
    const sections = document.querySelectorAll('section');
    
    // Add click event to email
    const contactEmail = document.querySelector('.contact-email');
    if (contactEmail) {
        contactEmail.addEventListener('click', function() {
            window.location.href = 'mailto:' + this.textContent;
        });
    }

    // Add scroll-to-top button
    const scrollToTopBtn = document.createElement('button');
    scrollToTopBtn.innerHTML = '↑';
    scrollToTopBtn.className = 'scroll-to-top';
    document.body.appendChild(scrollToTopBtn);

    // Style the scroll-to-top button
    const style = document.createElement('style');
    style.textContent = `
        .scroll-to-top {
            position: fixed;
            bottom: 20px;
            right: 20px;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background-color: #3498db;
            color: white;
            border: none;
            cursor: pointer;
            font-size: 20px;
            display: none;
            box-shadow: 0 2px 5px rgba(0,0,0,0.2);
            transition: background-color 0.3s;
        }
        .scroll-to-top:hover {
            background-color: #2980b9;
        }
    `;
    document.head.appendChild(style);

    // Show/hide scroll-to-top button
    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 300) {
            scrollToTopBtn.style.display = 'block';
        } else {
            scrollToTopBtn.style.display = 'none';
        }
    });

    // Scroll to top functionality
    scrollToTopBtn.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });

    // Add section highlighting on scroll
    const observerOptions = {
        root: null,
        rootMargin: '0px',
        threshold: 0.1
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.boxShadow = '0 4px 8px rgba(0, 0, 0, 0.1)';
            } else {
                entry.target.style.boxShadow = '0 2px 4px rgba(0, 0, 0, 0.05)';
            }
        });
    }, observerOptions);

    sections.forEach(section => {
        observer.observe(section);
    });
}); 