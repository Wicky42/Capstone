const Footer: React.FC = () => (
    <footer>
        <div className={"footer__brand"}>
            {/*Insert Logo ehre*/}
        </div>
        <div style={{marginTop: '1rem'}}>
            &copy; {new Date().getFullYear()} My Capstone Project. All rights reserved.
        </div>
    </footer>
);

export default Footer;
