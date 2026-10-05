import './Footer.css'

function Footer() {
    return (
        <footer className="footer">
            <p>
                Dados e imagens de jogos fornecidos por{' '}
                <a
                    href="https://rawg.io"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    RAWG
                </a>
                .
            </p>
        </footer>
    )
}

export default Footer